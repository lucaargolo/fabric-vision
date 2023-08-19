package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BookshelfSpeakerBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.*
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationPropertyHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.stream.Stream


class BookshelfSpeakerBlock(settings: Settings) : AbstractSpeakerBlock(4.0, settings), BlockEntityProvider {

    init {
        defaultState = defaultState.with(ROTATION, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(ROTATION)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultState.with(SkullBlock.ROTATION, RotationPropertyHelper.fromYaw(ctx.playerYaw-180f))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("state.with(SkullBlock.ROTATION, rotation.rotate((state.get(ROTATION)), MAX_ROTATIONS))", "net.minecraft.block.SkullBlock", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.ROTATION", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.MAX_ROTATIONS"))
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(SkullBlock.ROTATION, rotation.rotate(state[ROTATION], MAX_ROTATIONS))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("state.with(SkullBlock.ROTATION, mirror.mirror((state.get(ROTATION)), MAX_ROTATIONS))", "net.minecraft.block.SkullBlock", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.ROTATION", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.MAX_ROTATIONS"))
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.with(SkullBlock.ROTATION, mirror.mirror(state[ROTATION], MAX_ROTATIONS))
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = BookshelfSpeakerBlockEntity(pos, state)
    @Deprecated("Deprecated in Java", ReplaceWith("getShape(state[ROTATION])", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.getShape", "io.github.lucaargolo.fabricvision.common.block.ProjectorBlock.Companion.ROTATION"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[ROTATION])
    }

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.ENTITYBLOCK_ANIMATED", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState) = BlockRenderType.ENTITYBLOCK_ANIMATED

    companion object {
        private val MAX_ROTATION_INDEX = RotationPropertyHelper.getMax()
        private val MAX_ROTATIONS = MAX_ROTATION_INDEX + 1
        val ROTATION: IntProperty = Properties.ROTATION

        private val MAIN_SHAPE = VoxelShapes.combineAndSimplify(
            createCuboidShape(5.0, 0.0, 4.5, 11.0, 8.0, 5.5),
            createCuboidShape(5.0, 0.0, 5.5, 11.0, 8.0, 11.5),
        BooleanBiFunction.OR)

        private val SHAPES = mutableMapOf<Int, VoxelShape>()

        fun getShape(rotation: Int): VoxelShape {
            return SHAPES.getOrPut(rotation) {
                val yaw = RotationPropertyHelper.toDegrees(rotation) + 0.0
                MAIN_SHAPE.rotate(Direction.fromRotation(yaw))
            }
        }


    }

}