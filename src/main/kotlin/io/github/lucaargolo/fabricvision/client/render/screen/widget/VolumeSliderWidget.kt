package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToInt

class VolumeSliderWidget(private val parent: MediaPlayerScreen, x: Int, y: Int) : PlayerSliderWidget(x, y, 33, 140, 6, 0, 82, { parent.blockEntity.volume }) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderButton(context, mouseX, mouseY, delta)
        active = !parent.config
        if(active && (isHovered || isDragged)) {
            //TODO: Also translate this
            parent.playerTooltip.add(Text.literal("Volume: ").formatted(getFormatting(value)).append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY)).asOrderedText())
        }
    }

    override fun applyValue() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        buf.writeInt(0)
        buf.writeFloat(value.toFloat())
        ClientPlayNetworking.send(PacketCompendium.SET_VALUE_BUTTON_C2S, buf)
    }

    companion object {
        fun getFormatting(value: Double): Formatting {
            return when {
                value > 0.9 -> Formatting.RED
                value > 0.7 -> Formatting.YELLOW
                else -> Formatting.GREEN
            }
        }

    }


}