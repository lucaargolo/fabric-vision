package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToLong

class ProgressSliderWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int) : PlayerSliderWidget(x, y, 166, 5, 30, 0, 78, { (parent.mediaTime / parent.mediaDuration.toDouble()).toFloat().coerceIn(0f, 1f) }) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderButton(context, mouseX, mouseY, delta)
        active = !parent.config
        if(active && (isHovered || isDragged)) {
            val valueFromMouse = (mouseX - (x + 4.0)) / (width - 8.0)
            val draggingMediaTime = (valueFromMouse * parent.mediaDuration).roundToLong()
            parent.playerTooltip.add(Text.literal("Set video time to ").formatted(Formatting.GRAY).append(Text.literal(MediaPlayerScreen.formatTimestamp(draggingMediaTime)).styled { s -> s.withColor(0x00AFE4) }).asOrderedText())
        }
    }

    override fun applyValue() {
        if(!isDragged) {
            val draggingMediaTime = (value * parent.mediaDuration).roundToLong()
            val difference = ((draggingMediaTime - parent.mediaTime) / (parent.blockEntity.rate.toDouble())).roundToLong()
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(parent.blockEntity.pos)
            buf.writeLong(parent.startTime - difference)
            ClientPlayNetworking.send(PacketCompendium.SET_TIME_BUTTON_C2S, buf)
            parent.mediaTime = draggingMediaTime
            parent.changedProgressCooldown = 20
        }
    }

}