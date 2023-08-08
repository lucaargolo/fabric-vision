package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.UUID

class VideoDiskItem(settings: Settings) : Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if(user is ServerPlayerEntity) {
            if(!stack.orCreateNbt.contains("uuid")) {
                stack.orCreateNbt.putUuid("uuid", UUID.randomUUID())
            }
            val uuid = stack.orCreateNbt.getUuid("uuid")
            val buf = PacketByteBufs.create()
            buf.writeUuid(uuid)
            buf.writeEnumConstant(hand)
            ServerPlayNetworking.send(user, PacketCompendium.OPEN_VIDEO_DISK_SCREEN_S2C, buf)
        }
        return TypedActionResult.success(stack)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val mrl = stack.nbt?.let { if(it.contains("mrl")) it.getString("mrl") else "" } ?: ""
        val text = if(mrl.isNotEmpty()) Text.literal("Mrl: ").append(mrl) else Text.literal("No Media")
        tooltip.add(text)
    }

}