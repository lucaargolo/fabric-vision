package io.github.lucaargolo.fabricvision.player.callback

import com.sun.jna.Pointer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaSoundInstance
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.*
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.callback.AudioCallback
import javax.sound.sampled.AudioFormat

class MinecraftAudioCallback(val player: MinecraftMediaPlayer): AudioCallback {

    //TODO: Add support to stereo audio when audioMaxDist == 0
    private val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 128000f, 16, 1, 4, 128000f, false)

    private val instance = MinecraftMediaSoundInstance(player::volume)
    private var sourceManager: Channel.SourceManager? = null
    override fun play(mediaPlayer: MediaPlayer, samples: Pointer, sampleCount: Int, pts: Long) {
        val buffer = samples.getByteBuffer(0L, sampleCount * 2L)
        soundExecutor.execute {
            val volume = soundSystem.getAdjustedVolume(instance)
            if(volume > 0f && soundSystem.started && (sourceManager == null || sourceManager?.isStopped == true)) {
                val source = soundEngine.createSource(SoundEngine.RunMode.STREAMING)
                if (source != null) {
                    val channelSource = soundChannel.SourceManager(source)
                    soundChannel.sources.add(channelSource)
                    sourceManager = channelSource
                }
            }
        }
        sourceManager?.run { source ->
            source.setPosition(player.pos)
            if(player.audioMaxDist > 0f) {
                AL10.alSourcei(source.pointer, AL10.AL_DISTANCE_MODEL, AL11.AL_LINEAR_DISTANCE)
                AL10.alSourcef(source.pointer, AL10.AL_MAX_DISTANCE, player.audioMaxDist)
                AL10.alSourcef(source.pointer, AL10.AL_ROLLOFF_FACTOR, 1.0f)
                AL10.alSourcef(source.pointer, AL10.AL_REFERENCE_DISTANCE, player.audioRefDist)
            }
            val adjustedVolume = soundSystem.getAdjustedVolume(instance)
            if (adjustedVolume > 0.0f) {
                source.setVolume(adjustedVolume)
                source.removeProcessedBuffers()
                StaticSound(buffer, format).takeStreamBufferPointer().ifPresent { buffer ->
                    AL10.alSourceQueueBuffers(source.pointer, intArrayOf(buffer))
                }
                if(!source.isPlaying) {
                    source.play()
                }
            } else {
                clearSource()
            }
        }
    }

    override fun pause(mediaPlayer: MediaPlayer, pts: Long) {
        clearSource()
    }

    override fun resume(mediaPlayer: MediaPlayer, pts: Long) {
        clearSource()
    }

    override fun flush(mediaPlayer: MediaPlayer, pts: Long) {
        clearSource()
    }

    override fun drain(mediaPlayer: MediaPlayer) {
        clearSource()
    }

    override fun setVolume(volume: Float, mute: Boolean) = Unit

    private fun clearSource() {
        sourceManager?.run {
            it.stop()
        }
        if (soundChannel.sources.remove(sourceManager)) {
            sourceManager?.close()
        }
    }

    companion object {

        private val client = MinecraftClient.getInstance()

        private val soundManager = client.soundManager
        private val soundSystem = soundManager.soundSystem

        private val soundEngine = soundSystem.soundEngine
        private val soundExecutor = soundSystem.taskQueue

        val soundChannel = Channel(soundEngine, soundExecutor)

    }

}