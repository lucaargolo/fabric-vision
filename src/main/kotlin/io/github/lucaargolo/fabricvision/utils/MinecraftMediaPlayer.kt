package io.github.lucaargolo.fabricvision.utils

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.jni.JNINativeInterface
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.State
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class MinecraftMediaPlayer private constructor(val uuid: UUID, var mrl: String) {

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

    var visible = true
    var playing = false
    var time = 0L

    var identifier: Identifier = TRANSPARENT
        private set

    private var player: MediaPlayer? = null
    private var texture: NativeImageBackedTexture? = null

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
        if (status == Status.WAITING) {
            if (CREATING == null && LOADING == null) {
                CREATING = this
                status = Status.CREATING
                FACTORY.submit {
                    println("Creating player $uuid")
                    val mediaPlayer = FACTORY.mediaPlayers().newEmbeddedMediaPlayer()
                    FACTORY.videoSurfaces().newVideoSurface(MinecraftBufferCallback(), MinecraftRenderCallback(), true).let(mediaPlayer.videoSurface()::set)
                    player = mediaPlayer
                }
            }
        }
        //Maybe this isn't necessary and we can just delete the State.WAITING and finish the CREATING step inside the Factory.submit?
        if (status == Status.CREATING) {
            val mediaPlayer = player ?: return
            val state = mediaPlayer.status().state()
            if (state == State.NOTHING_SPECIAL) {
                if (CREATING == this) {
                    CREATING = null
                }
                load(true)
            }
        }
        if (status == Status.LOADING) {
            if (CREATING == null && LOADING == null) {
                val mediaPlayer = player ?: return
                LOADING = this
                mediaPlayer.submit {
                    status = if (mediaPlayer.media().startPaused(mrl)) {
                        currentMrl = mrl
                        Status.LOADED
                    } else {
                        Status.NO_MEDIA
                    }
                }
            }
        }
        if (status == Status.LOADED) {
            val mediaPlayer = player ?: return
            val validMedia = mediaPlayer.media().isValid
            status = if (validMedia) {
                Status.PAUSED_VISIBLE
            } else {
                Status.NO_MEDIA
            }
        }
        if (status.acceptMedia) {
            if (LOADING == this) {
                LOADING = null
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
                if(mediaPlayer.status().isPlaying) {
                    status = status.opposite.invoke() ?: Status.PAUSED_VISIBLE
                    mediaPlayer.controls().pause()
                }else{
                    status = status.opposite.invoke() ?: Status.PLAYING_VISIBLE
                    mediaPlayer.controls().play()
                }
            }
        }
    }

    fun close() {
        texture?.image = null
        texture?.clearGlId()
        status = Status.CLOSED
        val mediaPlayer = player ?: return
        mediaPlayer.submit {
            println("Cleaning player $uuid")
            if(mediaPlayer.status().isPlaying) {
                mediaPlayer.controls().pause()
            }
            mediaPlayer.release()
        }
    }

    inner class MinecraftBufferCallback: BufferFormatCallback {
        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            if(texture == null) {
                println("Creating texture $uuid")
                RenderSystem.recordRenderCall {
                    texture = NativeImageBackedTexture(1, 1, true)
                    identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", texture)
                }
            }
            if(sourceWidth != texture?.image?.width || sourceHeight != texture?.image?.height) texture?.let{ texture ->
                println("Updating image $uuid (${sourceWidth}x${sourceHeight})")
                RenderSystem.recordRenderCall {
                    texture.image = NativeImage(NativeImage.Format.RGBA, sourceWidth, sourceHeight, true)
                    TextureUtil.prepareImage(texture.glId, sourceWidth, sourceHeight)
                }
            }
            return BufferFormat("RGBA", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
        }
        override fun allocatedBuffers(buffers: Array<ByteBuffer>) {}
    }

    inner class MinecraftRenderCallback: RenderCallback {
        override fun display(player: MediaPlayer, buffers: Array<ByteBuffer>, bufferFormat: BufferFormat) {
            if (status.visible && buffers.size == 1) RenderSystem.recordRenderCall { texture?.image?.let { image ->
                val address = JNINativeInterface.GetDirectBufferAddress(buffers[0])
                MemoryUtil.memCopy(address, image.pointer, image.width * image.height * 4L)
                texture?.upload()
            } }
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
        PLAYING_INVISIBLE(true, true, false, true, { PLAYING_INVISIBLE }),   // mp ready, media playing, texture not updating
        TOO_FAR(true, true, false),                                                 // mp ready, media paused, texture not updating (cause player is too far/too many videos)
        CLOSED(false, false, false)                                                 // mp closed
    }

    companion object {
        val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")

        private var CREATING: MinecraftMediaPlayer? = null
        private var LOADING: MinecraftMediaPlayer? = null
        private val PLAYERS = mutableMapOf<UUID, MinecraftMediaPlayer>()
        private val FACTORY: MediaPlayerFactory by lazy {
            MediaPlayerFactory("--quiet")
        }

        private var tickedWorld = false

        fun worldTick() {
            if(!tickedWorld) {
                tickedWorld = true
                PLAYERS.values.forEach(MinecraftMediaPlayer::worldTick)
            }
        }

        fun clientTick() {
            val iterator = PLAYERS.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.value.clientTick()
                if(entry.value.status == Status.CLOSED) {
                    iterator.remove()
                }
            }
            tickedWorld = false
        }

        fun create(uuid: UUID): MinecraftMediaPlayer {
            val player = MinecraftMediaPlayer(uuid, "")
            PLAYERS[uuid] = player
            return player
        }

        fun close(stop: Boolean = true) {
            val iterator = PLAYERS.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.value.close()
            }
            if(stop) {
                FACTORY.release()
            }
        }
    }

}