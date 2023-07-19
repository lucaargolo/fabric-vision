package io.github.lucaargolo.fabricvision.utils

import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

object VoxelShapeUtils {

    fun VoxelShape.rotate(to: Direction): VoxelShape = rotate(Direction.NORTH, to)
    fun VoxelShape.rotate(from: Direction, to: Direction): VoxelShape {
        val buffer = arrayOf(this, VoxelShapes.empty())
        val times: Int = (to.horizontal - from.horizontal + 4) % 4
        for (i in 0 until times) {
            buffer[0].forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
            }
            buffer[0] = buffer[1]
            buffer[1] = VoxelShapes.empty()
        }
        return buffer[0]
    }

}