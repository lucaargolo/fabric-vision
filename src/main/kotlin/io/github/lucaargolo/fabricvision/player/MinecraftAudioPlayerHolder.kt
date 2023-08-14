package io.github.lucaargolo.fabricvision.player

import net.minecraft.client.MinecraftClient
import java.util.*

object MinecraftAudioPlayerHolder {

    private val PLAYERS = mutableSetOf<MinecraftAudioPlayer>()

    fun clientTick(client: MinecraftClient) {
        val iterator = PLAYERS.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            if(client.isIntegratedServerRunning) {
                if(client.isPaused && !entry.clientPaused) {
                    entry.clientPaused = true
                }else if(!client.isPaused && entry.clientPaused){
                    entry.clientPaused = false
                }
            }
        }

    }

    fun create(uuid: UUID): MinecraftAudioPlayer {
        val player = MinecraftAudioPlayer(uuid)
        PLAYERS.add(player)
        return player
    }

    fun close() {
        val iterator = PLAYERS.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.close()
        }
    }

}