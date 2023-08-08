package io.github.lucaargolo.fabricvision.common.blockentity

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f

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

    override fun getCenterPos(): Vec3d {
        val startPos = Vector3f(0.5f + (width / 2f), 1.0f + (height / 2f), 0.5f)
        val offsetPos = Vector3f(offsetX, offsetY, offsetZ)
        val yawRot = RotationAxis.POSITIVE_Y.rotationDegrees(yaw)
        val pitchRot = RotationAxis.POSITIVE_X.rotationDegrees(pitch)
        startPos.rotate(yawRot).rotate(pitchRot)
        offsetPos.rotate(yawRot).rotate(pitchRot)
        startPos.add(offsetPos)
        return Vec3d(startPos).add(Vec3d.of(pos))
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