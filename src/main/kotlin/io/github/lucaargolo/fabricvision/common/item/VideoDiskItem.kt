package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.utils.ModConfig
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.*

class VideoDiskItem(settings: Settings) : DiskItem(Type.VIDEO, settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if(user is ServerPlayerEntity) {
            if(!stack.orCreateNbt.contains("uuid")) {
                stack.orCreateNbt.putUuid("uuid", UUID.randomUUID())
                stack.orCreateNbt.putString("options", ModConfig.instance.defaultMediaOptions)
            }
        }
        return super.use(world, user, hand)
    }

}