package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.*

open class DiskItem(val type: Type, settings: Settings) : Item(settings) {

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
            buf.writeEnumConstant(type)
            ServerPlayNetworking.send(user, PacketCompendium.OPEN_DISK_SCREEN_S2C, buf)
        }
        return TypedActionResult.success(stack)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        val mrl = stack.nbt?.let { if(it.contains("mrl")) it.getString("mrl") else "" } ?: ""
        val showMrl = if(mrl.isEmpty()) Text.translatable("tooltip.fabricvision.status.no_media") else if (mrl.length > 32) Text.literal("..." + mrl.substring(mrl.length - 29, mrl.length)) else Text.literal(mrl)
        val name = stack.nbt?.let { if(it.contains("name")) it.getString("name") else "" } ?: ""
        val showName = if(name.isEmpty()) Text.translatable("tooltip.fabricvision.no_name") else Text.literal(name)
        val realType = stack.nbt?.let { if(it.contains("stream") && it.getBoolean("stream")) Type.STREAM else type } ?: type
        tooltip.add(Text.translatable("tooltip.fabricvision.name", showName.formatted(Formatting.GRAY)).formatted(realType.color))
        tooltip.add(Text.translatable(realType.mediaLocationKey, showMrl.formatted(Formatting.GRAY)).formatted(realType.color))
    }

    enum class Type(val color: Formatting, val mediaLocationKey: String) {
        NONE(Formatting.GRAY, ""),
        VIDEO(Formatting.BLUE, "tooltip.fabricvision.mrl"),
        AUDIO(Formatting.GREEN, "tooltip.fabricvision.mrl"),
        IMAGE(Formatting.YELLOW, "tooltip.fabricvision.mrl"),
        STREAM(Formatting.RED, "tooltip.fabricvision.stream");

        val model = ModelIdentifier(ModIdentifier(name.lowercase()+"_disk_model"), "inventory")
    }
    
}