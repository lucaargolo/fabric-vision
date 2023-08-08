package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.PanelBlockEntity
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import kotlin.jvm.optionals.getOrNull

class PanelBlock(settings: Settings) : HorizontalFacingMediaPlayerBlock<PanelBlockEntity>({ BlockEntityCompendium.PANEL }, settings) {

    override fun getOriginalPos(world: WorldAccess, state: BlockState, pos: BlockPos): BlockPos? {
        return world.getBlockEntity(pos, BlockEntityCompendium.PANEL).getOrNull()?.activePanelPos
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = PanelBlockEntity(pos, state)

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.getBlockEntity(pos, BlockEntityCompendium.PANEL).ifPresent { panelBlockEntity ->
                panelBlockEntity.setup(serverWorld, state[FACING], pos)
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if(!state.isOf(newState.block) && world is ServerWorld) {
            getBlockEntity(world, state, pos)?.let {
                it.diskStack?.let { diskStack ->
                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, diskStack)
                }
                it.diskStack = null
            }
            world.getBlockEntity(pos, BlockEntityCompendium.PANEL).ifPresent {
                it.diskStack?.let { diskStack ->
                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, diskStack)
                }
                it.diskStack = null
                it.activePanel?.disable(world)
                val facing = state[FACING]
                Direction.values().forEach { direction ->
                    if(direction.axis != facing.axis) {
                        val nearbyState = world.getBlockState(pos.offset(direction))
                        if(nearbyState.isOf(BlockCompendium.PANEL) && nearbyState[FACING] == facing) {
                            world.getBlockEntity(pos.offset(direction), BlockEntityCompendium.PANEL).ifPresent { nearbyPanel ->
                                if(!nearbyPanel.isRemoved && nearbyPanel.activePanelPos == null) {
                                    nearbyPanel.setup(world, facing, pos.offset(direction))
                                }
                            }
                        }
                    }
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

    @Deprecated("Deprecated in Java", ReplaceWith("ProjectorBlock.getShape(state[FACING])", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return getShape(state[FACING])
    }

    companion object {

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