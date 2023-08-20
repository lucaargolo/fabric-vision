package io.github.lucaargolo.fabricvision.network

import io.github.lucaargolo.fabricvision.client.render.screen.DiskScreen
import io.github.lucaargolo.fabricvision.client.render.screen.ImageDiskScreen
import io.github.lucaargolo.fabricvision.client.render.screen.VideoDiskScreen
import io.github.lucaargolo.fabricvision.common.blockentity.HologramBlockEntity
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.common.sound.SoundCompendium
import io.github.lucaargolo.fabricvision.client.CameraHelper
import io.github.lucaargolo.fabricvision.utils.ImgurHelper
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import io.github.lucaargolo.fabricvision.utils.ModLogger
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.text.Text
import net.minecraft.util.Hand
import kotlin.concurrent.thread
import kotlin.math.roundToInt

object PacketCompendium {

    val UPDATE_VIDEO_DISK_C2S = ModIdentifier("update_video_disk_c2s")
    val UPDATE_DISK_C2S = ModIdentifier("update_disk_c2s")
    val CLEAR_IMAGE_DISK_C2S = ModIdentifier("clear_image_disk_c2s")

    val SET_VALUE_BUTTON_C2S = ModIdentifier("set_value_button_c2s")
    val SET_TIME_BUTTON_C2S = ModIdentifier("set_time_button_c2s")
    val SET_RATE_BUTTON_C2S = ModIdentifier("set_rate_button_c2s")
    val ENABLE_BUTTON_C2S = ModIdentifier("enable_button_c2s")
    val REPEAT_BUTTON_C2S = ModIdentifier("repeat_button_c2s")
    val PLAY_BUTTON_C2S = ModIdentifier("play_button_c2s")

    val SEND_CAMERA_PIC_C2S = ModIdentifier("send_camera_pic_c2s")

    fun initialize() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_VIDEO_DISK_C2S) { server, player, handler, buf, sender ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            val name = buf.readString()
            val mrl = buf.readString()
            val options = buf.readString()
            val stream = buf.readBoolean()
            server.execute {
                val stack = player.getStackInHand(hand)
                if(stack.isOf(ItemCompendium.VIDEO_DISK) && stack.nbt?.getUuid("uuid") == uuid) {
                    stack.orCreateNbt.putString("name", name)
                    stack.orCreateNbt.putString("mrl", mrl)
                    stack.orCreateNbt.putString("options", options)
                    stack.orCreateNbt.putBoolean("stream", stream)
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_DISK_C2S) { server, player, handler, buf, sender ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            val type = buf.readEnumConstant(Type::class.java)
            val name = buf.readString()
            val mrl = buf.readString()
            server.execute {
                val stack = player.getStackInHand(hand)
                val valid = when(type) {
                    Type.AUDIO -> stack.isOf(ItemCompendium.AUDIO_DISK)
                    Type.IMAGE -> stack.isOf(ItemCompendium.IMAGE_DISK)
                    else -> false
                }
                if(valid && stack.nbt?.getUuid("uuid") == uuid) {
                    stack.orCreateNbt.putString("name", name)
                    stack.orCreateNbt.putString("mrl", mrl)
                }
            }
        }
        ServerPlayNetworking.registerGlobalReceiver(CLEAR_IMAGE_DISK_C2S) { server, player, handler, buf, sender ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            val delete = buf.readBoolean()
            server.execute {
                val stack = player.getStackInHand(hand)
                val valid = stack.isOf(ItemCompendium.IMAGE_DISK) && stack.nbt?.contains("delete") == true
                if(valid && stack.nbt?.getUuid("uuid") == uuid) {
                    if(delete) {
                        val deleteHash = stack.nbt?.getString("delete") ?: ""
                        if(deleteHash.isNotEmpty()) {
                            thread {
                                try {
                                    ImgurHelper.delete(deleteHash)
                                }catch (e: Exception) {
                                    ModLogger.warn("Couldn't delete Imgur picture", e)
                                }
                            }
                        }
                    }
                    player.setStackInHand(hand, ItemCompendium.BLANK_DISK.defaultStack)
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
        ServerPlayNetworking.registerGlobalReceiver(SEND_CAMERA_PIC_C2S) { server, player, handler, buf, sender ->
            val link = buf.readString()
            val delete = buf.readString()
            server.execute {
                val inventory = player.inventory
                var success = false
                repeat(inventory.size()) { slot ->
                    val stack = inventory.getStack(slot)
                    if(!stack.isEmpty && stack.isOf(ItemCompendium.BLANK_DISK)) {
                        success = true
                        stack.decrement(1)
                        return@repeat
                    }
                }
                if(success) {
                    val stack = ItemCompendium.IMAGE_DISK.defaultStack
                    val nbt = stack.orCreateNbt
                    nbt.putString("mrl", link)
                    nbt.putString("delete", delete)
                    inventory.offerOrDrop(stack)
                }
                val receiveBuf = PacketByteBufs.create()
                receiveBuf.writeBoolean(success)
                ServerPlayNetworking.send(player, RECEIVE_CAMERA_PIC_S2C, receiveBuf)
            }
        }
    }

    val OPEN_DISK_SCREEN_S2C = ModIdentifier("open_disk_screen_s2c")
    val RECEIVE_CAMERA_PIC_S2C = ModIdentifier("receive_camera_pic_s2c")

    fun initializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OPEN_DISK_SCREEN_S2C) { client, _, buf, _ ->
            val uuid = buf.readUuid()
            val hand = buf.readEnumConstant(Hand::class.java)
            val type = buf.readEnumConstant(Type::class.java)
            client.execute {
                val player = client?.player ?: return@execute
                val stack = player.getStackInHand(hand)
                when(type) {
                    Type.VIDEO -> client.setScreen(VideoDiskScreen(uuid, stack))
                    Type.AUDIO -> client.setScreen(DiskScreen(uuid, stack, type, Text.translatable("screen.fabricvision.title.audio_disk")))
                    Type.IMAGE -> client.setScreen(ImageDiskScreen(uuid, stack))
                    else -> Unit
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(RECEIVE_CAMERA_PIC_S2C) { client, _, buf, _ ->
            val success = buf.readBoolean()
            client.execute {
                if(success) {
                    client.soundManager.play(PositionedSoundInstance.master(SoundCompendium.DISK_EXTRACT, 1.0f))
                    CameraHelper.savedPicture = 100
                }else{
                    CameraHelper.errorSavingPicture = 100
                }
            }
        }
    }


}