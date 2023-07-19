package io.github.lucaargolo.fabricvision.utils

import net.minecraft.client.MinecraftClient
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

object MediaPlayerHolder {

    private val FACTORY: MediaPlayerFactory by lazy {
        MediaPlayerFactory()
    }

    private val PLAYERS = mutableMapOf<UUID, Pair<MediaPlayer, (MediaPlayer?) -> Unit>>()
    private val KEEP_ALIVE = mutableMapOf<UUID, Int>()

    val LOCK = AtomicBoolean(false)

    fun addPlayer(uuid: UUID, formatCallback: BufferFormatCallback, renderCallback: RenderCallback, returnCallback: (MediaPlayer?) -> Unit) {
        if(!LOCK.get()) {
            LOCK.set(true)
            FACTORY.submit {
                println("Creating player $uuid")
                val mediaPlayer = FACTORY.mediaPlayers().newEmbeddedMediaPlayer()
                val videoSurface = FACTORY.videoSurfaces().newVideoSurface(formatCallback, renderCallback, true)
                mediaPlayer.videoSurface().set(videoSurface)
                mediaPlayer.events().addMediaPlayerEventListener(object: MediaPlayerEventListenerImpl() {
                    override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
                        LOCK.set(false)
                    }
                })
                MinecraftClient.getInstance().execute {
                    PLAYERS[uuid] = mediaPlayer to returnCallback
                    KEEP_ALIVE[uuid] = 100
                    returnCallback.invoke(mediaPlayer)
                }
                LOCK.set(false)
            }
        }
    }

    fun cleanPlayer(uuid: UUID) {
        PLAYERS[uuid]?.let { (mediaPlayer, returnCallback) ->
            mediaPlayer.submit {
                println("Cleaning player $uuid")
                returnCallback.invoke(null)
                if(mediaPlayer.status().isPlaying) {
                    mediaPlayer.controls().pause()
                }
                mediaPlayer.release()
            }
        }
        PLAYERS.remove(uuid)
    }

    fun isAlive(uuid: UUID): Boolean {
        return KEEP_ALIVE.containsKey(uuid)
    }

    fun keepAlive(uuid: UUID) {
        if(KEEP_ALIVE.containsKey(uuid)) {
            KEEP_ALIVE[uuid] = 100
        }
    }

    fun tick() {
        val iterator = KEEP_ALIVE.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if(entry.value <= 0) {
                cleanPlayer(entry.key)
                iterator.remove()
            }else{
                entry.setValue(entry.value-1)
            }
        }
    }

    fun start() {
        FACTORY.submit {}
    }

    fun stop() {
        val iterator = PLAYERS.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val (mediaPlayer, returnCallback) = entry.value
            mediaPlayer.submit {
                returnCallback.invoke(null)
                if(mediaPlayer.status().isPlaying) {
                    mediaPlayer.controls().pause()
                }
                mediaPlayer.release()
            }
            iterator.remove()
        }
        FACTORY.release()
    }

}