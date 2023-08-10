package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector2i

class RateButtonWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int, private val rate: Float, private val textureU: Int): ButtonWidget(x, y, 7, 7, Text.empty(), {  }, DEFAULT_NARRATION_SUPPLIER) {

    private val textureV: Int
        get() = if(parent.blockEntity.rate == rate || isHovered || isFocused) 247 else 232

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU, textureV, 7, 7)
        if(isHovered) {
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            val text = Text.translatable("screen.fabricvision.message.set_rate", Text.literal("${rate}x").styled { s -> s.withColor(0x00AFE4) }).formatted(Formatting.GRAY)
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, listOf(text.asOrderedText()), { _, _, x, y, _, _ -> Vector2i(x, y) }, 6 + mouseX * 2, -10 + mouseY * 2)
            context.matrices.pop()

        }
    }

    override fun onPress() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        buf.writeFloat(rate)
        ClientPlayNetworking.send(PacketCompendium.SET_RATE_BUTTON_C2S, buf)
    }

}