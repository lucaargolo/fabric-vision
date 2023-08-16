package io.github.lucaargolo.fabricvision.player

import java.util.*

object MinecraftImagePlayerHolder {

    private val PLAYERS = mutableSetOf<MinecraftImagePlayer>()

    fun create(uuid: UUID): MinecraftImagePlayer {
        val player = MinecraftImagePlayer(uuid)
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