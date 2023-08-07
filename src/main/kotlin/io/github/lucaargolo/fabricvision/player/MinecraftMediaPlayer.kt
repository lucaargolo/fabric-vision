package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.FabricVision
import io.github.lucaargolo.fabricvision.player.callback.MinecraftAudioCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftBufferCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftRenderCallback
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import uk.co.caprica.vlcj.media.MediaType
import uk.co.caprica.vlcj.media.Meta
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.State
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.roundToLong

class MinecraftMediaPlayer(val uuid: UUID, var mrl: String) {

    private val internalStatus = AtomicReference(Status.WAITING)
    var status
        get() = internalStatus.get()
        private set(value) {
            println("Updating $uuid state ${internalStatus.get()} -> $value")
            internalStatus.set(value)
        }

    private val internalMrl = AtomicReference("")

    var currentMrl
        get() = internalMrl.get()
        private set(value) {
            println("Updating $uuid mrl ${internalMrl.get()} -> $value")
            internalMrl.set(value)
        }

    var shouldRenew = false

    var pos = Vec3d.ZERO
    var playing = false
    var repeating = false
    var rate = 1f
    var startTime = 0L
    var forceTime = false

    var audioMaxDist = 0f
    var audioRefDist = 0f

    var volume = 1f

    var identifier: Identifier = TRANSPARENT
    var texture: NativeImageBackedTexture? = null

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
                    player?.controls()?.play()
                }
            }
        }

    fun updateDuration(durationConsumer: (Long) -> Unit) {
        val mediaPlayer = player ?: return
        mediaPlayer.submit {
            durationConsumer.invoke(mediaPlayer.media().info().duration())
        }
    }

    fun updateTitle(titleConsumer: (String) -> Unit) {
        val mediaPlayer = player ?: return
        mediaPlayer.submit {
            if(status.interactable) {
                titleConsumer.invoke(mediaPlayer.media().meta().get(Meta.TITLE))
            }else{
                titleConsumer.invoke("")
            }
        }
    }

    fun worldTick() {
        val mediaPlayer = player ?: return
        if(clientPaused) {
            return
        }
        mediaPlayer.submit {
            if(status.interactable) {
                val mediaType = mediaPlayer.media().info().type()
                if(mediaType != MediaType.STREAM) {
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
                                mediaPlayer.controls().play()
                            }
                        }else if(mediaPlayer.status().isPlaying) {
                            mediaPlayer.controls().setPause(true)
                        }
                    }else if(status == Status.PAUSED) {
                        status = Status.PLAYING
                        if(!mediaPlayer.status().isPlaying) {
                            mediaPlayer.controls().play()
                        }
                    }else if(!mediaPlayer.status().isPlaying) {
                        mediaPlayer.controls().play()
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
            }else if(status.acceptMedia && mrl != currentMrl) {
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
                    println("Creating player $uuid")
                    val mediaPlayer = HOLDER.FACTORY!!.mediaPlayers().newEmbeddedMediaPlayer()
                    HOLDER.FACTORY!!.videoSurfaces().newVideoSurface(MinecraftBufferCallback(this), MinecraftRenderCallback(this), true).let(mediaPlayer.videoSurface()::set)
                    mediaPlayer.audio().callback("S16N", 128000, 1, MinecraftAudioCallback(this), true)
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
                    status = if (mediaPlayer.media().startPaused(mrl, ":avcodec-hw=any")) {
                        currentMrl = mrl
                        Status.LOADED
                    } else {
                        Status.NO_MEDIA
                    }
                }
            }
        }else if (status == Status.LOADED) {
            val mediaPlayer = player ?: return
            val validMedia = mediaPlayer.media().isValid
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

    fun load(justCreated: Boolean = false): Boolean {
        return if(justCreated || status.acceptMedia) {
            if (mrl.isEmpty()) {
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

    fun close(clearTexture: Boolean = true) {
        if(clearTexture) {
            texture?.image = null
            texture?.clearGlId()
        }
        status = if(player == null) {
            Status.CLOSED
        }else{
            Status.CLOSING
        }
        val mediaPlayer = player ?: return
        player = null
        mediaPlayer.submit {
            println("Cleaning player $uuid")
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

    enum class Status(val interactable: Boolean, val acceptMedia: Boolean, val formatting: Formatting) {
        NO_PLAYER(false, false, Formatting.RED),    // mmp not created, used for screen
        WAITING(false, false, Formatting.RED),      // mmp just created, waiting for liberation
        CREATING(false, false, Formatting.RED),     // mp requested
        NO_MEDIA(false, true, Formatting.YELLOW),   // mp ready, no media
        LOADING(false, false, Formatting.YELLOW),   // mp ready, media requested
        LOADED(false, true, Formatting.YELLOW),     // mp ready, media loaded
        PLAYING(true, true, Formatting.GREEN),      // mp ready, media playing
        PAUSED(true, true, Formatting.GREEN),       // mp ready, media paused
        STOPPED(true, true, Formatting.GREEN),      // mp ready, media stopped
        CLOSING(false, false, Formatting.RED),      // mp closing
        CLOSED(false, false, Formatting.RED);       // mp closed

        val translationKey: String
            get() = "tooltip.${FabricVision.MOD_ID}.status.${name.lowercase()}"

        val descriptionKey: String
            get() = "$translationKey.description"

    }

    companion object {
        val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")
        val HOLDER = MinecraftMediaPlayerHolder
    }


}