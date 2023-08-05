package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.common.blockentity.PanelBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class PanelBlock(settings: Settings) : MediaPlayerBlock({ BlockEntityCompendium.PANEL }, settings) {

    init {
        defaultState = defaultState.with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val direction = ctx.horizontalPlayerFacing.opposite
        return defaultState.with(FACING, direction)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = PanelBlockEntity(pos, state)

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.getBlockEntity(pos, BlockEntityCompendium.PANEL).ifPresent { panelBlockEntity ->
                panelBlockEntity.setup(serverWorld, state[FACING], pos)
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

    @Deprecated("Deprecated in Java", ReplaceWith("ProjectorBlock.getShape(state[FACING])", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[FACING])
    }

    companion object {

        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING

        private val MAIN_SHAPE = createCuboidShape(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)

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