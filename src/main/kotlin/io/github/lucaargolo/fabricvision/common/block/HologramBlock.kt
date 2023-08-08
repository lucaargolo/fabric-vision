package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.client.render.screen.HologramScreen
import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.HologramBlockEntity
import io.github.lucaargolo.fabricvision.common.blockentity.PanelBlockEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.stream.Stream

class HologramBlock(settings: Settings) : HorizontalFacingMediaPlayerBlock({ BlockEntityCompendium.HOLOGRAM }, settings) {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = HologramBlockEntity(pos, state)

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val originalPos = getOriginalPos(state, pos)
        world.getBlockEntity(originalPos, BlockEntityCompendium.HOLOGRAM).ifPresent {
            if(world.isClient) {
                MinecraftClient.getInstance().setScreen(HologramScreen(it))
            }
        }
        return ActionResult.SUCCESS
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        (world as? ServerWorld)?.let { serverWorld ->
            serverWorld.getBlockEntity(pos, BlockEntityCompendium.HOLOGRAM).ifPresent { hologramBlockEntity ->
                val facing = placer?.horizontalFacing ?: Direction.NORTH
                hologramBlockEntity.yaw = if(facing.axis == Direction.Axis.X) facing.opposite.asRotation() else facing.asRotation()
            }
        }
    }


    @Deprecated("Deprecated in Java", ReplaceWith("BlockRenderType.MODEL", "net.minecraft.block.BlockRenderType"))
    override fun getRenderType(state: BlockState) = BlockRenderType.MODEL

    @Deprecated("Deprecated in Java", ReplaceWith("SHAPE", "io.github.lucaargolo.fabricvision.common.block.HologramBlock.Companion.SHAPE"))
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return SHAPE
    }

    companion object {

        val SHAPE = Stream.of(
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
            createCuboidShape(0.0, 5.0, 0.0, 16.0, 6.0, 16.0),
            createCuboidShape(1.0, 3.0, 1.0, 15.0, 5.0, 15.0),
            createCuboidShape(0.0, 6.0, 0.0, 16.0, 8.0, 2.0),
            createCuboidShape(0.0, 6.0, 14.0, 16.0, 8.0, 16.0),
            createCuboidShape(0.0, 6.0, 2.0, 2.0, 8.0, 14.0),
            createCuboidShape(14.0, 6.0, 2.0, 16.0, 8.0, 14.0)
        ).reduce{ v1, v2 -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get();

    }

}