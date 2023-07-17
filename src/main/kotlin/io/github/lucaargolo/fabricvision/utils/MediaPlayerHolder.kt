package io.github.lucaargolo.fabricvision.utils

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.util.UUID

object MediaPlayerHolder {

    private val FACTORY: MediaPlayerFactory by lazy {
        MediaPlayerFactory()
    }

    private val PLAYERS = mutableMapOf<UUID, MediaPlayer>()
    private val KEEP_ALIVE = mutableMapOf<UUID, Int>()

    fun addPlayer(uuid: UUID, formatCallback: BufferFormatCallback, renderCallback: RenderCallback): MediaPlayer {
        println("Creating player ${uuid}")
        val mediaPlayer = FACTORY.mediaPlayers().newEmbeddedMediaPlayer()
        val videoSurface = FACTORY.videoSurfaces().newVideoSurface(formatCallback, renderCallback, true)
        mediaPlayer.videoSurface().set(videoSurface)
        PLAYERS[uuid] = mediaPlayer
        KEEP_ALIVE[uuid] = 100
        return mediaPlayer
    }
    fun cleanPlayer(uuid: UUID) {
        PLAYERS[uuid]?.let {
            it.submit {
                if(it.status().isPlaying) {
                    it.controls().pause()
                }
                it.release()
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
                println("Cleaning player ${entry.key}")
                cleanPlayer(entry.key)
                iterator.remove()
            }else{
                entry.setValue(entry.value-1)
            }
        }
        KEEP_ALIVE
    }

    fun start() {
        FACTORY.submit {}
    }

    fun stop() {
        val iterator = PLAYERS.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.release()
            iterator.remove()
        }
        FACTORY.release()
    }

}