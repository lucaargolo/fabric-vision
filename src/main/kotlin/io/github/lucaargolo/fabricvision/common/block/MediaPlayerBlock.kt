package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class MediaPlayerBlock(private val typeProvider: () -> BlockEntityType<out MediaPlayerBlockEntity>, settings: Settings) : BlockWithEntity(settings) {

    open fun getOriginalPos(state: BlockState, pos: BlockPos): BlockPos = pos

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(!world.isClient) {
            val originalPos = getOriginalPos(state, pos)
            world.getBlockEntity(originalPos, typeProvider.invoke()).ifPresent {
                if(player.isSneaking) it.play() else it.pause()
            }
        }
        return ActionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if(world.isClient)
            checkType(type, typeProvider.invoke(), MediaPlayerBlockEntity::clientTick)
        else
            checkType(type, typeProvider.invoke(), MediaPlayerBlockEntity::serverTick)
    }

}