package io.github.lucaargolo.fabricvision.player.callback

import com.sun.jna.Pointer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaSoundInstance
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.*
import org.lwjgl.openal.AL10
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.callback.AudioCallback
import javax.sound.sampled.AudioFormat

class MinecraftAudioCallback(mmp: MinecraftMediaPlayer): AudioCallback {

    private val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 128000f, 16, 1, 4, 128000f, false)

    private val instance = MinecraftMediaSoundInstance(mmp.pos)

    private var sourceManager: Channel.SourceManager? = null

    override fun play(mediaPlayer: MediaPlayer, samples: Pointer, sampleCount: Int, pts: Long) {
        val buffer = samples.getByteBuffer(0L, sampleCount * 2L)
        soundExecutor.execute {
            if(soundSystem.started && (sourceManager == null || sourceManager?.isStopped == true)) {
                val source = soundEngine.createSource(SoundEngine.RunMode.STREAMING)
                if (source != null) {
                    source.setAttenuation(instance.sound.attenuation + 0f)
                    source.setPosition(instance.pos)
                    source.setRelative(!instance.isRelative)
                    val channelSource = soundChannel.SourceManager(source)
                    soundChannel.sources.add(channelSource)
                    soundSystem.sources[instance] = channelSource
                    sourceManager = channelSource
                }
            }
        }
        sourceManager?.run { source ->
            source.removeProcessedBuffers()

            StaticSound(buffer, format).takeStreamBufferPointer().ifPresent { buffer ->
                AL10.alSourceQueueBuffers(source.pointer, intArrayOf(buffer))
            }

            if(!source.isPlaying) {
                source.play()
            }
        }
    }

    override fun pause(mediaPlayer: MediaPlayer, pts: Long) {
        sourceManager?.run {
            it.pause()
        }
    }

    override fun resume(mediaPlayer: MediaPlayer, pts: Long) {
        sourceManager?.run {
            it.resume()
        }
    }

    override fun flush(mediaPlayer: MediaPlayer, pts: Long) {
        if(soundSystem.sources.remove(instance) != null) {
            soundChannel.sources.remove(sourceManager)
            sourceManager?.close()
        }
    }

    override fun drain(mediaPlayer: MediaPlayer) {
    }

    override fun setVolume(volume: Float, mute: Boolean) {

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