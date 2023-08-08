package io.github.lucaargolo.fabricvision.common.blockentity

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class HologramBlockEntity(pos: BlockPos, state: BlockState) : MediaPlayerBlockEntity(BlockEntityCompendium.HOLOGRAM, pos, state) {

    var width = 16f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var height = 9f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var offsetX = -8f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var offsetY = 0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var offsetZ = 0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var yaw = 0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    var pitch = 0f
        set(value) {
            field = value
            markDirtyAndSync()
        }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putFloat("width", width)
        nbt.putFloat("height", height)
        nbt.putFloat("offsetX", offsetX)
        nbt.putFloat("offsetY", offsetY)
        nbt.putFloat("offsetZ", offsetZ)
        nbt.putFloat("yaw", yaw)
        nbt.putFloat("pitch", pitch)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        width = nbt.getFloat("width")
        height = nbt.getFloat("height")
        offsetX = nbt.getFloat("offsetX")
        offsetY = nbt.getFloat("offsetY")
        offsetZ = nbt.getFloat("offsetZ")
        yaw = nbt.getFloat("yaw")
        pitch  = nbt.getFloat("pitch")
    }


}