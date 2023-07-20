package io.github.lucaargolo.fabricvision.utils

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import com.sun.jna.Pointer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundEngine
import net.minecraft.client.sound.Source
import net.minecraft.client.sound.StaticSound
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.jni.JNINativeInterface
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.State
import uk.co.caprica.vlcj.player.base.callback.AudioCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.AudioFormat

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

    var shouldRenew = false

    var pos = Vec3d.ZERO
    var visible = true
    var playing = false
    var time = 0L

    var identifier: Identifier = TRANSPARENT
        private set

    private var player: MediaPlayer? = null
    private var texture: NativeImageBackedTexture? = null

    private var audioSource: Source? = null
    private var audioBuffer: StaticSound? = null

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
            if (CREATING == this && LOADING == null && ACTIVE_PLAYERS.size < MAX_SIMULTANEOUS_PLAYERS) {
                ACTIVE_PLAYERS.add(this)
                status = Status.CREATING
                FACTORY.submit {
                    println("Creating player $uuid")
                    val mediaPlayer = FACTORY.mediaPlayers().newEmbeddedMediaPlayer()
                    FACTORY.videoSurfaces().newVideoSurface(MinecraftBufferCallback(), MinecraftRenderCallback(), true).let(mediaPlayer.videoSurface()::set)
                    mediaPlayer.audio().callback("S16N", 16000, 2, MinecraftAudioCallback(), true)
                    audioSource = MinecraftClient.getInstance().soundManager.soundSystem.soundEngine.createSource(SoundEngine.RunMode.STREAMING)
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
        }else if (CREATING == this) {
            CREATING = null
        }

        //Loading steps
        if (status == Status.LOADING) {
            if (CREATING == null && LOADING == null) {
                val mediaPlayer = player ?: return
                LOADING = this
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

    fun close(clearTexture: Boolean = true) {
        if(clearTexture) {
            texture?.image = null
            texture?.clearGlId()
            audioSource?.let {
                MinecraftClient.getInstance().soundManager.soundSystem.soundEngine.release(it)
            }
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
        ACTIVE_PLAYERS.remove(this)
        return if(shouldRenew) {
            //When players get disabled due to lack of resources they are marked for renew
            shouldRenew = false
            status = Status.WAITING
            false
        }else{
            true
        }
    }
    inner class MinecraftAudioCallback: AudioCallback {

        override fun play(mediaPlayer: MediaPlayer, samples: Pointer, sampleCount: Int, pts: Long) {
            if(audioSource?.isPlaying != true) {
                //audioBuffer?.close()
                val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000f, 16, 2, 0, 1f, false)
                audioBuffer = StaticSound(samples.getByteBuffer(0L, sampleCount * 4L), format)
                audioSource?.setBuffer(audioBuffer)
                audioSource?.play()
            }

        }

        override fun pause(mediaPlayer: MediaPlayer, pts: Long) {

        }

        override fun resume(mediaPlayer: MediaPlayer, pts: Long) {

        }

        override fun flush(mediaPlayer: MediaPlayer, pts: Long) {

        }

        override fun drain(mediaPlayer: MediaPlayer) {

        }

        override fun setVolume(volume: Float, mute: Boolean) {

        }

    }

    inner class MinecraftBufferCallback: BufferFormatCallback {
        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            if(texture == null) {
                println("Creating texture $uuid")
                RenderSystem.recordRenderCall {
                    texture = NativeImageBackedTexture(1, 1, true)
                    identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", texture)
                    if(sourceWidth != texture?.image?.width || sourceHeight != texture?.image?.height) texture?.let{ texture ->
                        println("Updating image $uuid (${sourceWidth}x${sourceHeight})")
                        texture.image = NativeImage(NativeImage.Format.RGBA, sourceWidth, sourceHeight, true)
                        TextureUtil.prepareImage(texture.glId, sourceWidth, sourceHeight)
                    }
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
        PLAYING_INVISIBLE(true, true, false, true, { PAUSED_INVISIBLE }),    // mp ready, media playing, texture not updating
        CLOSING(false, false, false),                                               // mp closed
        CLOSED(false, false, false)                                                 // mp closed
    }

    companion object {
        private const val MAX_SIMULTANEOUS_PLAYERS = 8

        val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")

        private var CREATING: MinecraftMediaPlayer? = null
        private var LOADING: MinecraftMediaPlayer? = null

        private val ACTIVE_PLAYERS = mutableSetOf<MinecraftMediaPlayer>()
        private val PLAYERS = mutableSetOf<MinecraftMediaPlayer>()

        private val FACTORY: MediaPlayerFactory by lazy {
            MediaPlayerFactory("--quiet")
        }

        private var tickedWorld = false
        private var age = 0

        fun worldTick() {
            if(!tickedWorld) {
                tickedWorld = true
                PLAYERS.forEach(MinecraftMediaPlayer::worldTick)
            }
        }

        fun clientTick(client: MinecraftClient) {

            var maxDistancePlayer: MinecraftMediaPlayer? = null
            var maxDistance = Double.MIN_VALUE
            var minDistancePlayer: MinecraftMediaPlayer? = null
            var minDistance = Double.MAX_VALUE

            val player = client.player

            if(CREATING?.status != Status.WAITING && CREATING?.status != Status.CREATING) {
                CREATING = null
            }
            if(LOADING?.status == Status.LOADING) {
                LOADING = null
            }

            val iterator = PLAYERS.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.clientTick()
                if(player != null) {
                    if(ACTIVE_PLAYERS.contains(entry)) {
                        val distance = entry.pos.distanceTo(player.pos)
                        if (distance > maxDistance) {
                            maxDistancePlayer = entry
                            maxDistance = distance
                        }
                    }
                    if(entry.status == Status.WAITING) {
                        val distance = entry.pos.distanceTo(player.pos)
                        if (distance < minDistance) {
                            minDistancePlayer = entry
                            minDistance = distance
                        }
                    }
                }

                if(entry.status == Status.CLOSED) {
                    if(entry.closed()) {
                        iterator.remove()
                    }
                }
            }

            if(CREATING != null && ACTIVE_PLAYERS.size >= MAX_SIMULTANEOUS_PLAYERS) {
                CREATING = null
            }

            if(CREATING == null && minDistancePlayer != null) {
                if(ACTIVE_PLAYERS.size >= MAX_SIMULTANEOUS_PLAYERS) {
                    if(maxDistance > minDistance && maxDistancePlayer != null && maxDistancePlayer.status != Status.CLOSING && !ACTIVE_PLAYERS.contains(minDistancePlayer) && ACTIVE_PLAYERS.contains(maxDistancePlayer)) {
                        println("Replacing player ${maxDistancePlayer.uuid} for ${minDistancePlayer.uuid}")
                        maxDistancePlayer.shouldRenew = true
                        maxDistancePlayer.close(clearTexture = false)
                        CREATING = minDistancePlayer
                    }
                }else{
                    CREATING = minDistancePlayer
                }
            }



            tickedWorld = false
            age++
        }

        fun create(uuid: UUID): MinecraftMediaPlayer {
            val player = MinecraftMediaPlayer(uuid, "")
            PLAYERS.add(player)
            return player
        }

        fun close(stop: Boolean = true) {
            val iterator = PLAYERS.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.close()
            }
            if(stop) {
                FACTORY.release()
            }
        }
    }

}