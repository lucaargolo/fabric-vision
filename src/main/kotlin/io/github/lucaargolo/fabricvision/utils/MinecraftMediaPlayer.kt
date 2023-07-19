package io.github.lucaargolo.fabricvision.utils

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
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

class MinecraftMediaPlayer private constructor(val uuid: UUID, mrl: String) {

    private val internalStatus = AtomicReference(Status.WAITING)
    var status
        get() = internalStatus.get()
        private set(value) {
            println("Updating $uuid state ${internalStatus.get()} -> $value")
            internalStatus.set(value)
        }

    private val internalMrl = AtomicReference("")

    var mrl
        get() = internalMrl.get()
        private set(value) {
            println("Updating $uuid mrl ${internalMrl.get()} -> $value")
            internalMrl.set(value)
        }

    var identifier: Identifier = TRANSPARENT
        private set

    private var player: MediaPlayer? = null
    private var texture: NativeImageBackedTexture? = null

    fun tick() {
        if(status == Status.WAITING) {
            if(CREATING == null && LOADING == null) {
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
        if(status == Status.CREATING) {
            val mediaPlayer = player ?: return
            val state = mediaPlayer.status().state()
            if(state == State.NOTHING_SPECIAL) {
                if(CREATING == this) {
                    CREATING = null
                }
                load(mrl, true)
            }
        }
        if(status == Status.LOADED) {
            val mediaPlayer = player ?: return
            val validMedia = mediaPlayer.media().isValid
            status = if(validMedia) {
                Status.PAUSED
            }else{
                Status.NO_MEDIA
            }
        }
        if(LOADING == this && status.acceptMedia) {
            LOADING = null
        }
    }

    fun load(newMrl: String, justCreated: Boolean = false): Boolean {
        if(justCreated || status.acceptMedia) {
            val mediaPlayer = player ?: return false
            if (newMrl.isEmpty()) {
                status = Status.NO_MEDIA
                return false
            } else {
                LOADING = this
                status = Status.LOADING
                mediaPlayer.submit {
                    status = if (mediaPlayer.media().startPaused(newMrl)) {
                        mrl = newMrl
                        Status.LOADED
                    } else {
                        Status.NO_MEDIA
                    }
                }
                return true
            }
        }else{
            return false
        }
    }

    fun play() {
        if(status.interactable) {
            val mediaPlayer = player ?: return
            mediaPlayer.submit {
                if(mediaPlayer.status().isPlaying) {
                    status = Status.PAUSED
                    mediaPlayer.controls().pause()
                }else{
                    status = Status.PLAYING_VISIBLE
                    mediaPlayer.controls().play()
                }
            }
        }
    }

    fun close() {
        texture?.close()
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
            if (status.updateTexture && buffers.size == 1) texture?.image?.let { image ->
                image.pointer = JNINativeInterface.GetDirectBufferAddress(buffers[0])
                texture?.upload()
            }
        }

    }

    enum class Status(val interactable: Boolean, val acceptMedia: Boolean, val updateTexture: Boolean) {
        WAITING(false, false, false),         // mmp just created, waiting for liberation
        CREATING(false, false, false),        // mp requested
        NO_MEDIA(false, true, false),         // mp ready, no media
        LOADING(false, false, false),         // mp ready, media requested
        LOADED(false, true, false),           // mp ready, media loaded
        PAUSED(true, true, false),            // mp ready, media paused, texture not updating
        PLAYING_VISIBLE(true, true, true),    // mp ready, media playing, texture updating
        PLAYING_INVISIBLE(true, true, false), // mp ready, media playing, texture not updating
        TOO_FAR(true, true, false),           // mp ready, media paused, texture not updating (cause player is too far/too many videos)
        CLOSED(false, false, false)           // mp closed
    }

    companion object {
        private val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")

        private var CREATING: MinecraftMediaPlayer? = null
        private var LOADING: MinecraftMediaPlayer? = null
        private val PLAYERS = mutableMapOf<UUID, MinecraftMediaPlayer>()
        private val FACTORY: MediaPlayerFactory by lazy {
            MediaPlayerFactory()
        }

        fun tick() {
            val iterator = PLAYERS.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.value.tick()
                if(entry.value.status == Status.CLOSED) {
                    iterator.remove()
                }
            }
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