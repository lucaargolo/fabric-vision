package io.github.lucaargolo.fabricvision.network

import io.github.lucaargolo.fabricvision.client.render.screen.VideoDiskScreen
import io.github.lucaargolo.fabricvision.common.blockentity.HologramBlockEntity
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Hand
import kotlin.math.roundToInt

object PacketCompendium {

    val UPDATE_VIDEO_DISK_C2S = ModIdentifier("update_video_disk_c2s")

    val SET_VALUE_BUTTON_C2S = ModIdentifier("set_value_button_c2s")
    val SET_TIME_BUTTON_C2S = ModIdentifier("set_time_button_c2s")
    val SET_RATE_BUTTON_C2S = ModIdentifier("set_rate_button_c2s")
    val ENABLE_BUTTON_C2S = ModIdentifier("enable_button_c2s")
    val REPEAT_BUTTON_C2S = ModIdentifier("repeat_button_c2s")
    val PLAY_BUTTON_C2S = ModIdentifier("play_button_c2s")

    fun initialize() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_VIDEO_DISK_C2S) { server, player, handler, buf, sender ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            val mrl = buf.readString()
            val options = buf.readString()
            val stream = buf.readBoolean()
            server.execute {
                val stack = player.getStackInHand(hand)
                if(stack.isOf(ItemCompendium.VIDEO_DISK) && stack.nbt?.getUuid("uuid") == uuid) {
                    stack.orCreateNbt.putString("mrl", mrl)
                    stack.orCreateNbt.putString("options", options)
                    stack.orCreateNbt.putBoolean("stream", stream)
                }
            }
        }
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
                (player.world.getBlockEntity(pos) as? HologramBlockEntity)?.let { blockEntity ->
                    when(index) {
                        6 -> blockEntity.width = value.toInt().toFloat()
                        7 -> blockEntity.height = value.toInt().toFloat()
                        8 -> blockEntity.offsetX = (value*2).toInt()/2f
                        9 -> blockEntity.offsetY = (value*2).toInt()/2f
                        10 -> blockEntity.offsetZ = (value*2).toInt()/2f
                        11 -> blockEntity.yaw = value.roundToInt()*10f
                        12 -> blockEntity.pitch = value.roundToInt()*10f
                        13 -> blockEntity.width = value
                        14 -> blockEntity.height = value
                        15 -> blockEntity.offsetX = value
                        16 -> blockEntity.offsetY = value
                        17 -> blockEntity.offsetZ = value
                        18 -> blockEntity.yaw = value
                        19 -> blockEntity.pitch = value
                    }
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(SET_RATE_BUTTON_C2S) { server, player, handler, buf, sender ->
            val pos = buf.readBlockPos()
            val rate = buf.readFloat()
            server.execute {
                (player.world.getBlockEntity(pos) as? MediaPlayerBlockEntity)?.let { blockEntity ->
                    blockEntity.rate = rate
                    if(blockEntity.playing) {
                        blockEntity.play()
                    }else{
                        blockEntity.play()
                        blockEntity.pause()
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
                    blockEntity.changeEnable(enabled)
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
                        //TODO: Clean buffer?
                        blockEntity.pause()
                    }
                }
            }
        }
    }

    val OPEN_VIDEO_DISK_SCREEN_S2C = ModIdentifier("open_video_disk_screen_s2c")

    fun initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_VIDEO_DISK_SCREEN_S2C) { client, handler, buf, sender ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            client.execute {
                val player = client?.player ?: return@execute
                val stack = player.getStackInHand(hand)
                client.setScreen(VideoDiskScreen(uuid, stack))
            }
        }
    }


}