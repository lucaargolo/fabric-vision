package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayerHolder
import io.github.lucaargolo.fabricvision.utils.FramebufferTexture
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.Camera
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

abstract class MediaPlayerBlockEntity(type: BlockEntityType<out MediaPlayerBlockEntity>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {

    var player: MinecraftMediaPlayer? = null
        get() {
            val uuid = uuid
            if(field == null && uuid != null && enabled) {
                field = MinecraftMediaPlayerHolder.create(uuid)
            }
            return field
        }

    var enabled = true

    var uuid: UUID? = null

    var mrl = "https://cdn.discordapp.com/attachments/253728532939669504/1131109086407163924/SaveTube.io-Rick_Astley_-_Never_Gonna_Give_You_Up_Official_Music_Video.mp4"
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var time = 0L
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var playing = true
        set(value) {
            field = value
            markDirtyAndSync()
        }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putBoolean("enabled", enabled)
        if(uuid != null) {
            nbt.putUuid("uuid", uuid)
        }
        nbt.putString("mrl", mrl)
        nbt.putLong("time", time)
        nbt.putBoolean("playing", playing)
    }

    override fun readNbt(nbt: NbtCompound) {
        enabled = nbt.getBoolean("enabled")
        uuid = if(nbt.contains("uuid")) {
            nbt.getUuid("uuid")
        }else{
            UUID.randomUUID()
        }
        mrl = nbt.getString("mrl")
        time = nbt.getLong("time")
        playing = nbt.getBoolean("playing")
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return NbtCompound().also(::writeNbt)
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this) {
            toInitialChunkDataNbt()
        }
    }

    fun markDirtyAndSync() {
        markDirty()
        sync()
    }
    fun sync() {
        (world as? ServerWorld)?.chunkManager?.markForUpdate(this.pos)
    }

    override fun markRemoved() {
        super.markRemoved()
        if(world?.isClient == true) {
            player?.close()
        }
    }

    companion object {

        fun clientTick(world: World, pos: BlockPos, state: BlockState, blockEntity: MediaPlayerBlockEntity) {
            val player = blockEntity.player
            if(player != null) {
                if(blockEntity.enabled) {
                    player.pos = Vec3d.ofCenter(pos)
                    player.mrl = blockEntity.mrl
                    player.time = blockEntity.time
                    player.playing = blockEntity.playing
                }else{
                    player.close()
                    blockEntity.player = null
                }
            }
        }

    }

}