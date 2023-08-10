package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class RepeatButtonWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int): ButtonWidget(x, y, 14, 14, Text.empty(), {  }, DEFAULT_NARRATION_SUPPLIER) {

    private val textureU: Int
        get() = if(parent.blockEntity.repeating) 196 else 182


    private val textureV: Int
        get() = if(active && (isHovered || isFocused)) 60 else 42

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU, textureV, 14, 14)
        active = !parent.config
        if(active && isHovered) {
            if(parent.blockEntity.repeating) {
                parent.playerTooltip.add(Text.translatable("screen.fabricvision.message.repeating_enabled").formatted(Formatting.GREEN).asOrderedText())
            }else{
                parent.playerTooltip.add(Text.translatable("screen.fabricvision.message.repeating_disabled").formatted(Formatting.RED).asOrderedText())
            }
        }
    }

    override fun onPress() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        buf.writeBoolean(!parent.blockEntity.repeating)
        ClientPlayNetworking.send(PacketCompendium.REPEAT_BUTTON_C2S, buf)
    }

}