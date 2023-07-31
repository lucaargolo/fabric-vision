package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.player.callback.MinecraftAudioCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftBufferCallback
import io.github.lucaargolo.fabricvision.player.callback.MinecraftRenderCallback
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.State
import java.util.*
import java.util.concurrent.atomic.AtomicReference

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
    var visible = true
    var playing = false
    var time = 0L

    var identifier: Identifier = TRANSPARENT
    var texture: NativeImageBackedTexture? = null

    private var player: MediaPlayer? = null

    fun worldTick() {
        if(status.acceptMedia) {
            if(status.interactable) {
                if((playing && !status.playing) || (!playing && status.playing)) {
                    play()
                }else if(playing) {
                    if(visible && !status.visible) {
                        status = Status.PLAYING_VISIBLE
                    }else if(!visible && status.visible) {
                        status = Status.PLAYING_INVISIBLE
                    }
                }
            }
            if(mrl != currentMrl) {
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
            val state = mediaPlayer.status().state()
            if (state == State.NOTHING_SPECIAL) {
                load(true)
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
                Status.PAUSED_VISIBLE
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

    fun play() {
        if(status.interactable) {
            val mediaPlayer = player ?: return
            mediaPlayer.submit {
                if(mediaPlayer.status().isPlaying && status.playing) {
                    status = status.opposite.invoke() ?: Status.PAUSED_VISIBLE
                    mediaPlayer.controls().pause()
                }else if(!mediaPlayer.status().isPlaying && !status.playing){
                    status = status.opposite.invoke() ?: Status.PLAYING_VISIBLE
                    mediaPlayer.controls().play()
                }
            }
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

    enum class Status(val interactable: Boolean, val acceptMedia: Boolean, val visible: Boolean, val playing: Boolean = false, val opposite: () -> (Status?) = { null }) {
        WAITING(false, false, false),                                               // mmp just created, waiting for liberation
        CREATING(false, false, false),                                              // mp requested
        NO_MEDIA(false, true, false),                                               // mp ready, no media
        LOADING(false, false, false),                                               // mp ready, media requested
        LOADED(false, true, false),                                                 // mp ready, media loaded
        PAUSED_VISIBLE(true, true, false, false, { PLAYING_VISIBLE }),       // mp ready, media paused, texture not updating
        PAUSED_INVISIBLE(true, true, false, false, { PLAYING_INVISIBLE }),   // mp ready, media paused, texture not updating
        PLAYING_VISIBLE(true, true, true, true, { PAUSED_VISIBLE }),         // mp ready, media playing, texture updating
        PLAYING_INVISIBLE(true, true, false, true, { PAUSED_INVISIBLE }),    // mp ready, media playing, texture not updating
        CLOSING(false, false, false),                                               // mp closed
        CLOSED(false, false, false)                                                 // mp closed
    }

    companion object {
        val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")
        val HOLDER = MinecraftMediaPlayerHolder
    }


}