package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer.Status
import io.github.lucaargolo.fabricvision.player.callback.MinecraftAudioCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftBufferCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftRenderCallback
import io.github.lucaargolo.fabricvision.utils.ModConfig
import io.github.lucaargolo.fabricvision.utils.ModLogger
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import uk.co.caprica.vlcj.media.Meta
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.State
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.roundToLong

class MinecraftMediaPlayer(override val uuid: UUID): MinecraftPlayer {

    override val type = Type.VIDEO

    private val internalStatus = AtomicReference(Status.WAITING)
    override var status
        get() = internalStatus.get()
        private set(value) {
            ModLogger.debug("Updating $uuid state ${internalStatus.get()} -> $value")
            internalStatus.set(value)
        }

    private val internalMrl = AtomicReference("")

    private var currentMrl
        get() = internalMrl.get()
        private set(value) {
            ModLogger.debug("Updating $uuid mrl ${internalMrl.get()} -> $value")
            nativeTexture?.image?.let {
                it.fillRect(0, 0, it.width, it.height, 0)
            }
            nativeTexture?.upload()
            internalMrl.set(value)
        }

    var shouldRenew = false

    var options = ModConfig.instance.defaultMediaOptions

    private val parsedOptions: Array<String>
        get() = options.split(" ").toTypedArray()

    override var mrl = ""
    override var pos = Vec3d.ZERO
    override var stream = false
    override var playing = false
    override var repeating = false
    override var rate = 1f
    override var startTime = 0L
    override var forceTime = false
    override var volume = 1f
    override var audioMaxDist = 0f
    override var audioRefDist = 0f

    var texture: Identifier = MinecraftPlayer.TRANSPARENT
    var nativeTexture: NativeImageBackedTexture? = null

    private var player: MediaPlayer? = null

    private var clientPausedTime = 0L
    var clientPaused = false
        set(value) {
            if(value && status == Status.PLAYING) {
                field = true
                clientPausedTime = System.currentTimeMillis()
                player?.submit {
                    player?.controls()?.setPause(true)
                }
            }else if(!value) {
                field = false
                val currentTime = System.currentTimeMillis()
                startTime += (currentTime - clientPausedTime)
                player?.submit {
                    if(stream) player?.media()?.start(currentMrl, *parsedOptions) else player?.controls()?.play()
                }
            }
        }

    override fun getTexture(tickDelta: Float): Identifier {
        return texture
    }

    override fun updateDuration(durationConsumer: (Long) -> Unit) {
        val mediaPlayer = player ?: return
        mediaPlayer.submit {
            if(status.interactable) {
                durationConsumer.invoke(mediaPlayer.media().info().duration())
            }else{
                durationConsumer.invoke(0L)
            }
        }
    }

    override fun updateTitle(titleConsumer: (String) -> Unit) {
        val mediaPlayer = player ?: return
        mediaPlayer.submit {
            if(status.interactable) {
                titleConsumer.invoke(mediaPlayer.media().meta().get(Meta.TITLE))
            }else{
                titleConsumer.invoke("")
            }
        }
    }

    override fun tick() {
        val mediaPlayer = player ?: return
        if(clientPaused) {
            return
        }
        mediaPlayer.submit {
            if(status.interactable) {
                if(!stream) {
                    val mediaRate = mediaPlayer.status().rate()
                    if(mediaRate != rate) {
                        mediaPlayer.controls().setRate(rate)
                    }
                    val mediaTime = mediaPlayer.media().info().duration()
                    val currentTime = System.currentTimeMillis()
                    if (status != Status.STOPPED || repeating || forceTime) {
                        var presentationTime = ((currentTime - startTime)*rate.toDouble()).roundToLong()
                        if(repeating && mediaTime > 0L) presentationTime %= mediaTime

                        val currentPresentationTime = mediaPlayer.status().time()
                        val difference = abs(presentationTime - currentPresentationTime)
                        if (difference > 2000 || (forceTime && difference > 100)) {
                            mediaPlayer.controls().setTime(presentationTime)
                            if(forceTime && playing && status != Status.PLAYING) {
                                status = Status.PLAYING
                            }
                        }
                        if (!repeating && ((currentTime - startTime)*rate.toDouble()).roundToLong() >= mediaTime) {
                            status = Status.STOPPED
                            mediaPlayer.controls().setPause(true)
                        }
                    }
                }else{
                    val mediaRate = mediaPlayer.status().rate()
                    if(mediaRate != 1f) {
                        mediaPlayer.controls().setRate(1f)
                    }
                }
                if(playing) {
                    if(status == Status.STOPPED) {
                        val currentTime = System.currentTimeMillis()
                        val difference = abs(((currentTime - startTime)*rate.toDouble()).roundToLong())
                        if (difference < 100 || repeating) {
                            status = Status.PLAYING
                            if(!mediaPlayer.status().isPlaying) {
                                if(stream) mediaPlayer.media().start(currentMrl, *parsedOptions) else mediaPlayer.controls().play()
                            }
                        }else if(mediaPlayer.status().isPlaying) {
                            mediaPlayer.controls().setPause(true)
                        }
                    }else if(status == Status.PAUSED) {
                        status = Status.PLAYING
                        if(!mediaPlayer.status().isPlaying) {
                            if(stream) mediaPlayer.media().start(currentMrl, *parsedOptions) else mediaPlayer.controls().play()
                        }
                    }else if(!mediaPlayer.status().isPlaying) {
                        if(stream) mediaPlayer.media().start(currentMrl, *parsedOptions) else mediaPlayer.controls().play()
                    }
                }else {
                    if(status == Status.PLAYING) {
                        status = Status.PAUSED
                        if(mediaPlayer.status().isPlaying) {
                            mediaPlayer.controls().setPause(true)
                        }
                    }else if(mediaPlayer.status().isPlaying) {
                        mediaPlayer.controls().setPause(true)
                    }
                }
            }
            if(status.acceptMedia && mrl != currentMrl) {
                load()
            }
        }
    }

    fun clientTick() {
        //Creation steps
        if (status == Status.WAITING) {
            if (HOLDER.CREATING == this && HOLDER.LOADING == null && HOLDER.ACTIVE_PLAYERS.size < HOLDER.MAX_SIMULTANEOUS_PLAYERS) {
                HOLDER.ACTIVE_PLAYERS.add(this)
                status = Status.CREATING
                HOLDER.FACTORY?.submit {
                    ModLogger.debug("Creating player $uuid")
                    val mediaPlayer = HOLDER.FACTORY!!.mediaPlayers().newEmbeddedMediaPlayer()
                    HOLDER.FACTORY!!.videoSurfaces().newVideoSurface(MinecraftBufferCallback(this), MinecraftRenderCallback(this), true).let(mediaPlayer.videoSurface()::set)
                    mediaPlayer.audio().callback("S16N", 64000, 1, MinecraftAudioCallback(this), true)
                    player = mediaPlayer
                }
            }
        } else if (status == Status.CREATING) {
            //Maybe this isn't necessary, and we can just delete the State.WAITING and finish the CREATING step inside the Factory.submit?
            val mediaPlayer = player ?: return
            mediaPlayer.submit {
                val state = mediaPlayer.status().state()
                if (state == State.NOTHING_SPECIAL) {
                    load(true)
                }
            }
        }else if (HOLDER.CREATING == this) {
            HOLDER.CREATING = null
        }

        //Loading steps
        if (status == Status.LOADING) {
            if (HOLDER.CREATING == null && HOLDER.LOADING == null) {
                val mediaPlayer = player ?: return
                HOLDER.LOADING = this
                mediaPlayer.submit {
                    status = if (mediaPlayer.media().start(currentMrl, *parsedOptions)) {
                        Status.LOADED
                    } else {
                        Status.NO_MEDIA
                    }
                }
            }
        }else if (status == Status.LOADED) {
            val mediaPlayer = player ?: return
            val validMedia = mediaPlayer.media().isValid
            mediaPlayer.controls().setPause(true)
            status = if (validMedia) {
                Status.PAUSED
            } else {
                Status.NO_MEDIA
            }
        }
        if (status.acceptMedia) {
            if (HOLDER.LOADING == this) {
                HOLDER.LOADING = null
            }
        }
    }

    private fun load(justCreated: Boolean = false): Boolean {
        player?.controls()?.stop()
        return if(justCreated || status.acceptMedia) {
            currentMrl = mrl
            if (currentMrl.isEmpty()) {
                status = Status.NO_MEDIA
                false
            } else {
                status = Status.LOADING
                true
            }
        }else{
            false
        }
    }

    override fun close() {
        close(true)
    }

    fun close(clearAssets: Boolean) {
        if(clearAssets) {
            nativeTexture?.image = null
            nativeTexture?.clearGlId()
        }
        status = if(player == null) {
            Status.CLOSED
        }else{
            Status.CLOSING
        }
        val mediaPlayer = player ?: return
        player = null
        mediaPlayer.submit {
            ModLogger.debug("Cleaning player $uuid")
            if(mediaPlayer.status().isPlaying) {
                mediaPlayer.controls().pause()
            }
            mediaPlayer.release()
            status = Status.CLOSED
        }
    }

    fun closed(): Boolean {
        HOLDER.ACTIVE_PLAYERS.remove(this)
        return if(shouldRenew) {
            //When players get disabled due to lack of resources they are marked for renew
            shouldRenew = false
            status = Status.WAITING
            false
        }else{
            true
        }
    }

    companion object {
        val HOLDER = MinecraftMediaPlayerHolder
    }


}