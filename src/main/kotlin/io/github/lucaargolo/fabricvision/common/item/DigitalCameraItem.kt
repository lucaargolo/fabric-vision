package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.client.CameraHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class DigitalCameraItem(settings: Settings) : Item(settings) {

    override fun getMaxUseTime(stack: ItemStack?): Int {
        return 1200
    }

    override fun getUseAction(stack: ItemStack?): UseAction {
        return UseAction.SPYGLASS
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?>? {
        if(world.isClient) {
            CameraHelper.digitalCameraFovMultiplier = 1f
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this))
        return ItemUsage.consumeHeldItem(world, user, hand)
    }

}