package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class SpeakerBlock(settings: Settings) : HorizontalFacingSpeakerBlock(8.0, settings) {

    @Deprecated("Deprecated in Java", ReplaceWith("ProjectorBlock.getShape(state[FACING])", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[HorizontalFacingMediaPlayerBlock.FACING])
    }

    companion object {

        private val MAIN_SHAPE = VoxelShapes.combineAndSimplify(
            createCuboidShape(2.0, 0.0, 1.0, 14.0, 16.0, 3.0),
            createCuboidShape(2.0, 0.0, 3.0, 14.0, 16.0, 15.0),
        BooleanBiFunction.OR)

        private val SHAPES = mapOf(
            Direction.NORTH to MAIN_SHAPE,
            Direction.EAST to MAIN_SHAPE.rotate(Direction.EAST),
            Direction.WEST to MAIN_SHAPE.rotate(Direction.WEST),
            Direction.SOUTH to MAIN_SHAPE.rotate(Direction.SOUTH),
        )

        fun getShape(facing: Direction): VoxelShape {
            return SHAPES[facing]!!
        }

    }



}