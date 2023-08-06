package io.github.lucaargolo.fabricvision.network

import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object PacketCompendium {

    val SET_VALUE_BUTTON_C2S = ModIdentifier("set_value_button_c2s")
    val SET_TIME_BUTTON_C2S = ModIdentifier("set_time_button_c2s")
    val ENABLE_BUTTON_C2S = ModIdentifier("enable_button_c2s")
    val REPEAT_BUTTON_C2S = ModIdentifier("repeat_button_c2s")
    val PLAY_BUTTON_C2S = ModIdentifier("play_button_c2s")

    fun initialize() {
        ServerPlayNetworking.registerGlobalReceiver(SET_VALUE_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val index = buf.readInt()
            val value = buf.readFloat()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    when(index) {
                        0 -> blockEntity.volume = value
                        1 -> blockEntity.light = value
                        2 -> blockEntity.red = value
                        3 -> blockEntity.green = value
                        4 -> blockEntity.blue = value
                        5 -> blockEntity.alpha = value
                    }
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(SET_TIME_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val time = buf.readLong()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    blockEntity.startTime = time
                    blockEntity.forceTime = true
                    blockEntity.forceTimeCooldown = 5
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(ENABLE_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val enabled = buf.readBoolean()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    blockEntity.enabled = enabled
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(REPEAT_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val repeat = buf.readBoolean()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    blockEntity.repeating = repeat
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(PLAY_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val play = buf.readBoolean()
            val stop = buf.readBoolean()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    if(play) {
                        blockEntity.play()
                    } else {
                        blockEntity.pause()
                    }
                    if(stop) {
                        blockEntity.pause()
                    }
                }
            }
        }
    }

    fun initializeClient() {

    }


}