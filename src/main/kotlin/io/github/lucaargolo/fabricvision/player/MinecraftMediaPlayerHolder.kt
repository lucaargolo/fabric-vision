package io.github.lucaargolo.fabricvision.player

import com.sun.jna.NativeLibrary
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import java.io.File
import java.util.*
import kotlin.io.path.pathString

object MinecraftMediaPlayerHolder {

    const val MAX_SIMULTANEOUS_PLAYERS = 8

    var CREATING: MinecraftMediaPlayer? = null
    var LOADING: MinecraftMediaPlayer? = null

    private val PLAYERS = mutableSetOf<MinecraftMediaPlayer>()
    val ACTIVE_PLAYERS = mutableSetOf<MinecraftMediaPlayer>()

    var FACTORY: MediaPlayerFactory? = null

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

        if(CREATING?.status != MinecraftMediaPlayer.Status.WAITING && CREATING?.status != MinecraftMediaPlayer.Status.CREATING) {
            CREATING = null
        }
        if(LOADING?.status != MinecraftMediaPlayer.Status.LOADING) {
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
                if(entry.status == MinecraftMediaPlayer.Status.WAITING) {
                    val distance = entry.pos.distanceTo(player.pos)
                    if (distance < minDistance) {
                        minDistancePlayer = entry
                        minDistance = distance
                    }
                }
            }

            if(entry.status == MinecraftMediaPlayer.Status.CLOSED) {
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
                if(maxDistance > minDistance && maxDistancePlayer != null && maxDistancePlayer.status != MinecraftMediaPlayer.Status.CLOSING && !ACTIVE_PLAYERS.contains(minDistancePlayer) && ACTIVE_PLAYERS.contains(maxDistancePlayer)) {
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
            FACTORY?.release()
        }
    }

}