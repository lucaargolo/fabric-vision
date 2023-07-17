package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.FlatScreenBlockEntity
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

class FlatScreenBlock(settings: Settings) : BlockWithEntity(settings){

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = FlatScreenBlockEntity(pos, state)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if(world.isClient) {
            world.getBlockEntity(pos, BlockEntityCompendium.FLAT_SCREEN).ifPresent {
                it.pause()
            }
        }
        return ActionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if(world.isClient) checkType(type, BlockEntityCompendium.FLAT_SCREEN, FlatScreenBlockEntity::clientTick) else null
    }


}