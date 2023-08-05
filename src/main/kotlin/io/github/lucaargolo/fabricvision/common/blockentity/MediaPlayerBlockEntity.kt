package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayerHolder
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
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

    private var uuid: UUID? = null

    var player: MinecraftMediaPlayer? = null
        get() {
            val uuid = uuid
            if(field == null && uuid != null && enabled) {
                field = MinecraftMediaPlayerHolder.create(uuid)
            }
            return field
        }

    protected var enabled = true

    protected var mrl = "C:\\Users\\Luca\\Downloads\\timer.webm"
        set(value) {
            field = value
            markDirtyAndSync()
        }


    protected var lastTime = System.currentTimeMillis()
    protected var startTime = System.currentTimeMillis()
        set(value) {
            field = value
            markDirtyAndSync()
        }

    protected var playing = true
        set(value) {
            field = value
            markDirtyAndSync()
        }

    protected var repeating = false
        set(value) {
            field = value
            markDirtyAndSync()
        }

    protected var audioMaxDist = 16.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    protected var audioRefDist = 0.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    protected var volume = 1.0f
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
        nbt.putLong("lastTime", lastTime)
        nbt.putLong("startTime", startTime)
        nbt.putBoolean("playing", playing)
        nbt.putBoolean("repeating", repeating)
        nbt.putFloat("audioMaxDist", audioMaxDist)
        nbt.putFloat("audioRefDist", audioRefDist)
        nbt.putFloat("volume", volume)
    }

    override fun readNbt(nbt: NbtCompound) {
        enabled = nbt.getBoolean("enabled")
        uuid = if(nbt.contains("uuid")) {
            nbt.getUuid("uuid")
        }else{
            UUID.randomUUID()
        }
        mrl = nbt.getString("mrl")
        lastTime = nbt.getLong("lastTime")
        startTime = nbt.getLong("startTime")
        playing = nbt.getBoolean("playing")
        repeating = nbt.getBoolean("repeating")
        audioMaxDist = nbt.getFloat("audioMaxDist")
        audioRefDist = nbt.getFloat("audioRefDist")
        volume = nbt.getFloat("volume")
    }

    open fun play() {
        playing = true
        startTime = System.currentTimeMillis()
    }

    open fun pause() {
        playing = !playing
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
        val world = world ?: return
        if(world.isClient) {
            player?.pos = Vec3d.ofCenter(pos)
            player?.mrl = mrl
            player?.startTime = startTime
            player?.playing = playing
            player?.repeating = repeating
            player?.audioMaxDist = audioMaxDist
            player?.audioRefDist = audioRefDist
            player?.volume = volume
        }else{
            (world as? ServerWorld)?.chunkManager?.markForUpdate(this.pos)
        }
    }

    override fun markRemoved() {
        super.markRemoved()
        if(world?.isClient == true) {
            player?.close()
        }
    }

    companion object {

        fun serverTick(world: World, pos: BlockPos, state: BlockState, blockEntity: MediaPlayerBlockEntity) {
            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - blockEntity.lastTime
            if(timeDifference > 100 || !blockEntity.playing) {
                blockEntity.startTime += timeDifference
            }
            blockEntity.lastTime = currentTime
        }

        fun clientTick(world: World, pos: BlockPos, state: BlockState, blockEntity: MediaPlayerBlockEntity) {
            if(blockEntity.player != null && !blockEntity.enabled) {
                blockEntity.player?.close()
                blockEntity.player = null
            }
        }

    }

}