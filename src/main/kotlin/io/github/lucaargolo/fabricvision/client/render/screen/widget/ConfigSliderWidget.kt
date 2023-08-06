package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector2i
import kotlin.math.roundToInt

class ConfigSliderWidget(private val parent: MediaPlayerScreen, x: Int, y: Int, textureU: Int, textureV: Int, barU: Int, barV: Int, value: Float, private val index: Int, private val tooltip: (Double) -> Text) : PlayerSliderWidget(x, y, 33, textureU, textureV, barU, barV, value) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderButton(context, mouseX, mouseY, delta)
        if(isHovered) {
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, listOf(tooltip.invoke(value).asOrderedText()), { _, _, x, y, _, _ -> Vector2i(x, y)}, 6+mouseX*2, -10+mouseY*2)
            context.matrices.pop()
        }
    }

    override fun applyValue() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        buf.writeInt(index)
        buf.writeFloat(value.toFloat())
        ClientPlayNetworking.send(PacketCompendium.SET_VALUE_BUTTON_C2S, buf)
    }


}