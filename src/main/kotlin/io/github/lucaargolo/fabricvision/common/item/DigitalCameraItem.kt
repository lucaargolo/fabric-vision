package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.sound.SoundEvents
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
            FabricVisionClient.digitalCameraFovMultiplier = 1f
        }
        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0f, 1.0f)
        user.incrementStat(Stats.USED.getOrCreateStat(this))
        return ItemUsage.consumeHeldItem(world, user, hand)
    }

    companion object {

        fun PlayerEntity.isUsingCamera(): Boolean {
            return (this.isUsingItem && this.activeItem.isOf(ItemCompendium.DIGITAL_CAMERA)) || (world.isClient && (FabricVisionClient.takePicture > 0 || FabricVisionClient.takingPicture || FabricVisionClient.takenPicture > 90 || FabricVisionClient.uploadingPicture || FabricVisionClient.uploadedPicture > 90))
        }

    }

}