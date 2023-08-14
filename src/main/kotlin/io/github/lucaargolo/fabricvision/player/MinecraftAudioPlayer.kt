package io.github.lucaargolo.fabricvision.player

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer.Status
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.Channel
import net.minecraft.client.sound.SoundEngine
import net.minecraft.client.sound.StaticSound
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.LockSupport
import javax.sound.sampled.AudioFormat
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.roundToLong

class MinecraftAudioPlayer(override val uuid: UUID): MinecraftPlayer {

    private val handler: EventHandler by lazy {
        EventHandler()
    }

    private val player = MANAGER.createPlayer().also {
        it.addListener(handler)
    }

    private var loading = false
    private var lastMrl = ""
    private var invalidMrl = false

    override val type = Type.AUDIO

    override val status
        get() = when {
            player.playingTrack == null -> Status.NO_MEDIA
            playing && !player.isPaused -> Status.PLAYING
            playing && player.isPaused -> Status.STOPPED
            else -> Status.PAUSED
        }

    override var mrl = ""
    override var pos = Vec3d.ZERO
    override var stream
        get() = player?.playingTrack?.info?.isStream ?: false
        set(value) = Unit

    override var playing = false
    override var repeating = false
    override var rate = 1f
    override var startTime = 0L
    override var forceTime = false
    override var volume = 1f
    override var audioMaxDist = 0f
    override var audioRefDist = 0f

    override var texture: Identifier = MinecraftMediaPlayer.TRANSPARENT

    private var clientPausedBackup = false
    var clientPaused = false
        set(value) {
            if(!field && value) {
                clientPausedBackup = player.isPaused
                player.isPaused = true
            }else if(field && !value) {
                player.isPaused = clientPausedBackup
            }
            field = value
        }

    override fun tick() {
        val mediaTime = player.playingTrack?.duration ?: 0L
        val currentTime = System.currentTimeMillis()
        var shouldUpdate = false
        var presentationTime = ((currentTime - startTime)*rate.toDouble()).roundToLong()
        if(player.isPaused && playing) {
            if(repeating && mediaTime > 0L) presentationTime %= mediaTime
            if(presentationTime < mediaTime) {
                player.isPaused = false
            }
        }else if(!player.isPaused && !playing) {
            player.isPaused = true
        }
        val currentPresentationTime = player.playingTrack?.position ?: presentationTime
        val difference = abs(presentationTime - currentPresentationTime)
        if (difference > 2000 || (forceTime && difference > 100)) {
            shouldUpdate = true
        }
        if(repeating && mediaTime > 0L) presentationTime %= mediaTime
        if(presentationTime < mediaTime && player.playingTrack != null) {
            if(shouldUpdate && player.playingTrack.state != AudioTrackState.SEEKING) {
                player.playingTrack.position = presentationTime
            }
        }else {
            player.isPaused = true
        }
        if(!loading) {
            if((player.playingTrack == null && !invalidMrl && presentationTime < mediaTime) || (mrl != lastMrl)) {
                player.stopTrack()
                loading = true
                invalidMrl = false
                MANAGER.loadItem(mrl, LoadHandler(presentationTime))
            }
        }
    }

    override fun updateDuration(durationConsumer: (Long) -> Unit) {
        durationConsumer.invoke(player.playingTrack?.duration ?: 0L)
    }

    override fun updateTitle(titleConsumer: (String) -> Unit) {
        titleConsumer.invoke(player.playingTrack?.info?.title ?: "")
    }

    override fun close() {
        player.destroy()
        handler.close()
    }

    inner class EventHandler: AudioEventAdapter() {

        private val format = AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44100f, 16, 2, 960, 44100f, false)
        private val instance = MinecraftMediaSoundInstance(::volume)
        private var sourceManager: Channel.SourceManager? = null

        @Volatile
        private var threadStop = false
        private var thread = thread {
            while (true) {
                val frame = player.provide()
                if (frame == null) {
                    if(threadStop) {
                        Thread.currentThread().interrupt()
                        break
                    }else{
                        LockSupport.parkNanos("lets wait a little", 200000L)
                    }
                }else {
                    val buffer = ByteBuffer.allocateDirect(frame.dataLength)
                    buffer.put(frame.data)
                    buffer.flip()
                    soundExecutor.execute {
                        val volume = soundSystem.getAdjustedVolume(instance)
                        if (volume > 0f && soundSystem.started && (sourceManager == null || sourceManager?.isStopped == true)) {
                            val source = soundEngine.createSource(SoundEngine.RunMode.STREAMING)
                            if (source != null) {
                                val channelSource = soundChannel.SourceManager(source)
                                soundChannel.sources.add(channelSource)
                                sourceManager = channelSource
                            }
                        }
                        sourceManager?.source?.let { source ->
                            source.setPosition(pos)
                            if (audioMaxDist > 0f) {
                                AL10.alSourcei(source.pointer, AL10.AL_DISTANCE_MODEL, AL11.AL_LINEAR_DISTANCE)
                                AL10.alSourcef(source.pointer, AL10.AL_MAX_DISTANCE, audioMaxDist)
                                AL10.alSourcef(source.pointer, AL10.AL_ROLLOFF_FACTOR, 1.0f)
                                AL10.alSourcef(source.pointer, AL10.AL_REFERENCE_DISTANCE, audioRefDist)
                            }
                            if (volume > 0.0f) {
                                source.setPitch(rate)
                                source.setVolume(volume)
                                source.removeProcessedBuffers()
                                StaticSound(buffer, format).takeStreamBufferPointer().ifPresent { buffer ->
                                    AL10.alSourceQueueBuffers(source.pointer, intArrayOf(buffer))
                                }
                                if (!source.isPlaying) {
                                    source.play()
                                }
                            } else {
                                clearSource()
                            }
                        }
                    }
                    if(threadStop) {
                        Thread.currentThread().interrupt()
                        break
                    }else{
                        Thread.sleep((20L/rate).roundToLong())
                    }
                }
            }
        }

        override fun onPlayerPause(player: AudioPlayer) {
            sourceManager?.run {
                it.pause()
            }
        }

        override fun onPlayerResume(player: AudioPlayer?) {
            sourceManager?.run {
                it.resume()
            }
        }

        override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
            println("track started")
            clearSource()
        }

        override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
            println("track ended with reason $endReason")
            clearSource()
            if(endReason == AudioTrackEndReason.FINISHED) {
                player.playTrack(track.makeClone())
            }
        }

        override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
            println("track exception")
            clearSource()
        }

        override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
            println("track stuck")
        }

        private fun clearSource() {
            sourceManager?.run {
                it.stop()
                if (soundChannel.sources.remove(sourceManager)) {
                    sourceManager?.close()
                }
            }
        }

        fun close() {
            threadStop = true
            clearSource()
        }

    }

    inner class LoadHandler(private val presentationTime: Long): AudioLoadResultHandler {

        private var fails = 0

        override fun trackLoaded(track: AudioTrack) {
            loading = false
            lastMrl = mrl
            player.stopTrack()
            if(presentationTime < track.duration) {
                track.position = presentationTime
                player.playTrack(track)
            }
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            loading = false
            lastMrl = mrl
            val track = playlist.tracks.firstOrNull()
            if(track != null) {
                player.stopTrack()
                player.playTrack(track)
            }else{
                invalidMrl = true
            }
        }

        override fun noMatches() {
            loading = false
            lastMrl = mrl
            invalidMrl = true
        }

        override fun loadFailed(exception: FriendlyException) {
            loading = false
            if(++fails >= 3) {
                lastMrl = mrl
                invalidMrl = true
            }
        }

    }

    companion object {

        private val MANAGER = DefaultAudioPlayerManager().also(AudioSourceManagers::registerRemoteSources).also {
            it.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_LE
            it.setUseSeekGhosting(false)
        }

        private val client = MinecraftClient.getInstance()

        private val soundManager = client.soundManager
        private val soundSystem = soundManager.soundSystem

        private val soundEngine = soundSystem.soundEngine
        private val soundExecutor = soundSystem.taskQueue

        val soundChannel = Channel(soundEngine, soundExecutor)

    }

}