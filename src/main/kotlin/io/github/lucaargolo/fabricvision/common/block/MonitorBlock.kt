package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.MonitorBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.stream.Stream

class MonitorBlock(settings: Settings) : HorizontalFacingMediaPlayerBlock<MonitorBlockEntity>({ BlockEntityCompendium.MONITOR }, settings) {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = MonitorBlockEntity(pos, state)

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

    @Deprecated("Deprecated in Java", ReplaceWith("MonitorBlock.getShape(state[FACING])", "io.github.lucaargolo.fabricvision.common.block.MonitorBlock.Companion.FACING"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[FACING])
    }

    companion object {

        private val MAIN_SHAPE = Stream.of(
            createCuboidShape(15.5, 5.0, 7.5, 16.0, 13.5, 8.5),
            createCuboidShape(7.5, 1.0, 7.5, 8.5, 3.0, 8.5),
            createCuboidShape(0.5, 5.0, 8.0, 15.5, 13.5, 9.0),
            createCuboidShape(0.0, 3.0, 7.5, 16.0, 5.0, 8.5),
            createCuboidShape(0.0, 13.5, 7.5, 16.0, 14.0, 8.5),
            createCuboidShape(0.0, 5.0, 7.5, 0.5, 13.5, 8.5),
            createCuboidShape(5.5, 0.0, 6.5, 10.5, 1.0, 9.5)
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get();

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