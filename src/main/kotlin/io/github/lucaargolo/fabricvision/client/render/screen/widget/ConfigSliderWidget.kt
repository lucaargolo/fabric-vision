package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import org.joml.Vector2i

class ConfigSliderWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int, width: Int, textureU: Int, textureV: Int, barU: Int, barV: Int, valueSupplier: () -> Float, private val index: Int, private val tooltip: (Double) -> Text, private val minValue: Double = 0.0, private val maxValue: Double = 1.0) : PlayerSliderWidget(x, y, width, textureU, textureV, barU, barV, { ((valueSupplier.invoke() - minValue)/(maxValue - minValue)).toFloat() }) {

    constructor(parent: MediaPlayerScreen<*>, x: Int, y: Int, textureU: Int, textureV: Int, barU: Int, barV: Int, valueSupplier: () -> Float, index: Int, tooltip: (Double) -> Text, minValue: Double = 0.0, maxValue: Double = 1.0): this(parent, x, y, 33, textureU, textureV, barU, barV, valueSupplier, index, tooltip, minValue, maxValue)

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderButton(context, mouseX, mouseY, delta)
        if (isHovered || isDragged) {
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, listOf(tooltip.invoke(MathHelper.lerp(value, minValue, maxValue)).asOrderedText()), { _, _, x, y, _, _ -> Vector2i(x, y) }, 6 + mouseX * 2, -10 + mouseY * 2)
            context.matrices.pop()
        }
    }

    override fun applyValue() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        buf.writeInt(index)
        buf.writeFloat(MathHelper.lerp(value, minValue, maxValue).toFloat())
        ClientPlayNetworking.send(PacketCompendium.SET_VALUE_BUTTON_C2S, buf)
    }


}