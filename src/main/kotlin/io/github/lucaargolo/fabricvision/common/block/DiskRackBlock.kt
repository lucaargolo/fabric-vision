package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.DiskRackBlockEntity
import io.github.lucaargolo.fabricvision.common.item.DiskItem
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.utils.VoxelShapeUtils.rotate
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.shape.VoxelShape

import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.stream.Stream


class DiskRackBlock(settings: Settings?) : BlockWithEntity(settings) {

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

    @Deprecated("Deprecated in Java", ReplaceWith("state.with(FACING, rotation.rotate(state.get(FACING) as Direction)) as BlockState", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "net.minecraft.util.math.Direction", "net.minecraft.block.BlockState"))
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(FACING, rotation.rotate(state.get(FACING) as Direction)) as BlockState
    }

    @Deprecated("Deprecated in Java", ReplaceWith("state.rotate(mirror.getRotation(state.get(FACING) as Direction))", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "net.minecraft.util.math.Direction"))
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(FACING) as Direction))
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            world.getBlockEntity(pos, BlockEntityCompendium.DISK_RACK).ifPresent { blockEntity ->
                blockEntity.stacks.forEach { diskStack ->
                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, diskStack)
                }
                blockEntity.stacks.clear()
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return DiskRackBlockEntity(pos, state)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(!world.isClient) {
            val facing = state[FACING]
            val rayPos = player.raycast(4.5, 1.0F, false).pos.subtract(pos.x + 0.0, pos.y + 0.0, pos.z + 0.0).multiply(16.0).toVector3f()
            rayPos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(facing.asRotation()))
            rayPos.absolute()
            if(facing.direction == Direction.AxisDirection.NEGATIVE) {
                if(facing.axis == Direction.Axis.X) {
                    rayPos.x = 16f - rayPos.x
                    rayPos.z = 16f - rayPos.z
                }
                if(facing.axis == Direction.Axis.Z) {
                    rayPos.z = 16f - rayPos.z
                }
            }
            val disk = if(rayPos.x in 4f..12f && rayPos.y in 1f..9f) {
                when(rayPos.z) {
                    in 1.5f..2.5f -> 0
                    in 3.5f..4.5f -> 1
                    in 5.5f..6.5f -> 2
                    in 7.5f..8.5f -> 3
                    in 9.5f..10.5f -> 4
                    in 11.5f..12.5f -> 5
                    in 13.5f..14.5f -> 6
                    else -> -1
                }
            }else -1
            val stack = player.getStackInHand(hand)
            if(disk >= 0) {
                world.getBlockEntity(pos, BlockEntityCompendium.DISK_RACK).ifPresent { blockEntity ->
                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, blockEntity.stacks[disk])
                    blockEntity.stacks[disk] = ItemStack.EMPTY
                    if(stack.item is DiskItem) {
                        blockEntity.stacks[disk] = stack.copy()
                        player.setStackInHand(hand, ItemStack.EMPTY)
                    }
                    blockEntity.markDirtyAndSync()
                }
            }

        }
        return ActionResult.SUCCESS
    }

    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

    @Deprecated("Deprecated in Java", ReplaceWith("DiskRackBlock.getShape(state[FACING])", "io.github.lucaargolo.fabricvision.common.block.DiskRackBlock.Companion.FACING"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        var shape = getShape(state[FACING])
        if(context.isHolding(ItemCompendium.VIDEO_DISK) || context.isHolding(ItemCompendium.AUDIO_DISK) || context.isHolding(ItemCompendium.IMAGE_DISK)) {
            shape = VoxelShapes.union(
                shape,
                getDiskShape(0, state[FACING]),
                getDiskShape(1, state[FACING]),
                getDiskShape(2, state[FACING]),
                getDiskShape(3, state[FACING]),
                getDiskShape(4, state[FACING]),
                getDiskShape(5, state[FACING]),
                getDiskShape(6, state[FACING])
            )
        }else{
            world.getBlockEntity(pos, BlockEntityCompendium.DISK_RACK).ifPresent { blockEntity ->
                blockEntity.stacks.forEachIndexed { disk, stack ->
                    if(!stack.isEmpty) {
                        shape = VoxelShapes.union(shape, getDiskShape(disk, state[FACING]))
                    }
                }
            }
        }
        return shape
    }

    companion object {

        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING

        private val MAIN_SHAPE = Stream.of(
            createCuboidShape(4.0, 0.0, 0.5, 12.0, 1.0, 15.5),
            createCuboidShape(10.0, 1.0, 12.5, 12.0, 2.0, 13.5),
            createCuboidShape(5.0, 2.0, 12.5, 11.0, 3.0, 13.5),
            createCuboidShape(4.0, 1.0, 12.5, 6.0, 2.0, 13.5),
            createCuboidShape(10.0, 1.0, 14.5, 12.0, 2.0, 15.5),
            createCuboidShape(5.0, 2.0, 14.5, 11.0, 3.0, 15.5),
            createCuboidShape(4.0, 1.0, 14.5, 6.0, 2.0, 15.5),
            createCuboidShape(4.0, 1.0, 10.5, 6.0, 2.0, 11.5),
            createCuboidShape(4.0, 1.0, 8.5, 6.0, 2.0, 9.5),
            createCuboidShape(5.0, 2.0, 10.5, 11.0, 3.0, 11.5),
            createCuboidShape(10.0, 1.0, 10.5, 12.0, 2.0, 11.5),
            createCuboidShape(5.0, 2.0, 8.5, 11.0, 3.0, 9.5),
            createCuboidShape(10.0, 1.0, 8.5, 12.0, 2.0, 9.5),
            createCuboidShape(4.0, 1.0, 10.5, 6.0, 2.0, 11.5),
            createCuboidShape(4.0, 1.0, 8.5, 6.0, 2.0, 9.5),
            createCuboidShape(5.0, 2.0, 10.5, 11.0, 3.0, 11.5),
            createCuboidShape(10.0, 1.0, 10.5, 12.0, 2.0, 11.5),
            createCuboidShape(5.0, 2.0, 8.5, 11.0, 3.0, 9.5),
            createCuboidShape(10.0, 1.0, 8.5, 12.0, 2.0, 9.5),
            createCuboidShape(10.0, 1.0, 4.5, 12.0, 2.0, 5.5),
            createCuboidShape(5.0, 2.0, 4.5, 11.0, 3.0, 5.5),
            createCuboidShape(4.0, 1.0, 4.5, 6.0, 2.0, 5.5),
            createCuboidShape(10.0, 1.0, 6.5, 12.0, 2.0, 7.5),
            createCuboidShape(5.0, 2.0, 6.5, 11.0, 3.0, 7.5),
            createCuboidShape(4.0, 1.0, 6.5, 6.0, 2.0, 7.5),
            createCuboidShape(4.0, 1.0, 2.5, 6.0, 2.0, 3.5),
            createCuboidShape(4.0, 1.0, 0.5, 6.0, 2.0, 1.5),
            createCuboidShape(5.0, 2.0, 2.5, 11.0, 3.0, 3.5),
            createCuboidShape(10.0, 1.0, 2.5, 12.0, 2.0, 3.5),
            createCuboidShape(5.0, 2.0, 0.5, 11.0, 3.0, 1.5),
            createCuboidShape(10.0, 1.0, 0.5, 12.0, 2.0, 1.5),
            createCuboidShape(4.0, 1.0, 2.5, 6.0, 2.0, 3.5),
            createCuboidShape(4.0, 1.0, 0.5, 6.0, 2.0, 1.5),
            createCuboidShape(5.0, 2.0, 2.5, 11.0, 3.0, 3.5),
            createCuboidShape(10.0, 1.0, 2.5, 12.0, 2.0, 3.5),
            createCuboidShape(5.0, 2.0, 0.5, 11.0, 3.0, 1.5),
            createCuboidShape(10.0, 1.0, 0.5, 12.0, 2.0, 1.5),
        ).reduce { v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()

        private val DISK_0_SHAPE = createCuboidShape(4.0, 1.0, 13.5, 12.0, 9.0, 14.5)
        private val DISK_1_SHAPE = createCuboidShape(4.0, 1.0, 11.5, 12.0, 9.0, 12.5)
        private val DISK_2_SHAPE = createCuboidShape(4.0, 1.0, 9.5, 12.0, 9.0, 10.5)
        private val DISK_3_SHAPE = createCuboidShape(4.0, 1.0, 7.5, 12.0, 9.0, 8.5)
        private val DISK_4_SHAPE = createCuboidShape(4.0, 1.0, 5.5, 12.0, 9.0, 6.5)
        private val DISK_5_SHAPE = createCuboidShape(4.0, 1.0, 3.5, 12.0, 9.0, 4.5)
        private val DISK_6_SHAPE = createCuboidShape(4.0, 1.0, 1.5, 12.0, 9.0, 2.5)

        private val DISK_SHAPES = listOf(DISK_0_SHAPE, DISK_1_SHAPE, DISK_2_SHAPE, DISK_3_SHAPE, DISK_4_SHAPE, DISK_5_SHAPE, DISK_6_SHAPE)

        private val SHAPES = mapOf(
            Direction.NORTH to MAIN_SHAPE,
            Direction.EAST to MAIN_SHAPE.rotate(Direction.EAST),
            Direction.WEST to MAIN_SHAPE.rotate(Direction.WEST),
            Direction.SOUTH to MAIN_SHAPE.rotate(Direction.SOUTH),
        )

        fun getDiskShape(index: Int, facing: Direction) = DISK_SHAPES.getOrNull(index)?.rotate(facing)

        fun getShape(facing: Direction): VoxelShape {
            return SHAPES[facing]!!
        }

    }


}