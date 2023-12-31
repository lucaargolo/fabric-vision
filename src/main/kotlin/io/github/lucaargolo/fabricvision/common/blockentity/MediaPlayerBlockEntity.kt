package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.common.item.DiskItem
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.player.*

import io.github.lucaargolo.fabricvision.utils.ModConfig
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

abstract class MediaPlayerBlockEntity(type: BlockEntityType<out MediaPlayerBlockEntity>, pos: BlockPos, state: BlockState) : SyncableBlockEntity(type, pos, state) {

    private var uuid: UUID? = null

    var diskStack: ItemStack? = null
    val type: Type
        get() = (diskStack?.item as? DiskItem)?.type ?: Type.NONE

    var player: MinecraftPlayer? = null
        get() {
            val uuid = uuid
            if(field == null && uuid != null && enabled) {
                field = when(type) {
                    Type.VIDEO -> MinecraftMediaPlayerHolder.create(uuid)
                    Type.AUDIO -> MinecraftAudioPlayerHolder.create(uuid)
                    Type.IMAGE -> MinecraftImagePlayerHolder.create(uuid)
                    else -> null
                }
            }
            return field
        }

    var forceTimeCooldown = 0

    var enabled = true
        private set(value) {
            field = value
            markDirtyAndSync()
        }

    private var mrl = ""
        set(value) {
            field = value
            markDirtyAndSync()
        }

    private var options = ModConfig.instance.defaultMediaOptions
        set(value) {
            field = value
            markDirtyAndSync()
        }

    private var lastTime = System.currentTimeMillis()

    var startTime = System.currentTimeMillis()
        set(value) {
            field = value
            if(playing) {
                markDirtyAndSync()
            }else{
                markDirty()
            }
        }

    var forceTime = false
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var playing = false
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var repeating = false
        set(value) {
            field = value
            markDirtyAndSync()
        }

    private var stream = false
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var rate = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    private var audioMaxDist = 16.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    private var audioRefDist = 0.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var volume = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var light = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var red = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var green = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var blue = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var alpha = 1.0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    open fun changeEnable(enabled: Boolean) {
        this.enabled = enabled
    }

    open fun getCenterPos(): Vec3d = Vec3d.ofCenter(pos)

    fun getDiskMrl(): String {
        return diskStack?.nbt?.let { if(it.contains("mrl")) it.getString("mrl") else null } ?: mrl
    }

    fun getDiskOptions(): String {
        return diskStack?.nbt?.let { if(it.contains("options")) it.getString("options") else null } ?: options
    }

    fun isStreamDisk(): Boolean {
        return diskStack?.nbt?.let { if(it.contains("stream")) it.getBoolean("stream") else null } ?: stream
    }

    fun isStreamInternal(): Boolean {
        return player?.stream ?: false
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putBoolean("enabled", enabled)
        if(uuid != null) {
            nbt.putUuid("uuid", uuid)
        }
        if(diskStack != null) {
            nbt.put("diskStack", diskStack?.writeNbt(NbtCompound()))
        }
        nbt.putString("mrl", mrl)
        nbt.putString("options", options)
        nbt.putBoolean("stream", stream)
        nbt.putLong("lastTime", lastTime)
        nbt.putLong("startTime", startTime)
        nbt.putBoolean("forceTime", forceTime)
        nbt.putBoolean("playing", playing)
        nbt.putBoolean("repeating", repeating)
        nbt.putFloat("rate", rate)
        nbt.putFloat("audioMaxDist", audioMaxDist)
        nbt.putFloat("audioRefDist", audioRefDist)
        nbt.putFloat("volume", volume)
        nbt.putFloat("light", light)
        nbt.putFloat("red", red)
        nbt.putFloat("green", green)
        nbt.putFloat("blue", blue)
        nbt.putFloat("alpha", alpha)
    }

    override fun readNbt(nbt: NbtCompound) {
        enabled = nbt.getBoolean("enabled")
        uuid = if(nbt.contains("uuid")) {
            nbt.getUuid("uuid")
        }else{
            UUID.randomUUID()
        }
        diskStack = if(nbt.contains("diskStack")) {
            ItemStack.fromNbt(nbt.getCompound("diskStack"))
        }else{
            null
        }
        mrl = nbt.getString("mrl")
        options = nbt.getString("options")
        stream = nbt.getBoolean("stream")
        lastTime = nbt.getLong("lastTime")
        startTime = nbt.getLong("startTime")
        forceTime = nbt.getBoolean("forceTime")
        playing = nbt.getBoolean("playing")
        repeating = nbt.getBoolean("repeating")
        rate = nbt.getFloat("rate")
        audioMaxDist = nbt.getFloat("audioMaxDist")
        audioRefDist = nbt.getFloat("audioRefDist")
        volume = nbt.getFloat("volume")
        light = nbt.getFloat("light")
        red = nbt.getFloat("red")
        green = nbt.getFloat("green")
        blue = nbt.getFloat("blue")
        alpha = nbt.getFloat("alpha")
    }

    open fun play() {
        playing = true
        startTime = System.currentTimeMillis()
    }

    open fun pause() {
        playing = !playing
    }

    override fun sync() {
        val world = world ?: return
        if (world.isClient) {
            updatePlayer()
        } else {
            (world as? ServerWorld)?.chunkManager?.markForUpdate(this.pos)
        }
    }

    fun updatePlayer() {
        if(type != Type.NONE && type != player?.type) {
            player?.close()
            player = null
        }
        (player as? MinecraftMediaPlayer)?.options = getDiskOptions()
        player?.mrl = getDiskMrl()
        player?.pos = getCenterPos()
        player?.playing = playing
        player?.stream = isStreamDisk()
        player?.startTime = startTime
        player?.forceTime = forceTime
        player?.repeating = repeating
        player?.rate = rate
        player?.audioMaxDist = audioMaxDist
        player?.audioRefDist = audioRefDist
        player?.volume = volume
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
            if(blockEntity.enabled) {
                val timeDifference = currentTime - blockEntity.lastTime
                if (timeDifference > 100 || !blockEntity.playing) {
                    blockEntity.startTime += timeDifference
                }
                blockEntity.lastTime = currentTime
                if (blockEntity.forceTime) {
                    if (blockEntity.forceTimeCooldown <= 0) {
                        blockEntity.forceTime = false
                    } else {
                        blockEntity.forceTimeCooldown--
                    }
                }
            }else{
                blockEntity.startTime = currentTime
                blockEntity.lastTime = currentTime
            }
        }

        fun clientTick(world: World, pos: BlockPos, state: BlockState, blockEntity: MediaPlayerBlockEntity) {
            val currentTime = System.currentTimeMillis()
            if(blockEntity.enabled) {
                if (!blockEntity.playing) {
                    blockEntity.startTime += currentTime - blockEntity.lastTime
                    blockEntity.lastTime = currentTime
                    blockEntity.updatePlayer()
                }
            }else{
                blockEntity.startTime = currentTime
                blockEntity.lastTime = currentTime
                if (blockEntity.player != null) {
                    blockEntity.player?.close()
                    blockEntity.player = null
                }
            }
            blockEntity.player?.tick()
        }

    }

}