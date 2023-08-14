package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToLong

class ProgressSliderWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int) : PlayerSliderWidget(x, y, 166, 5, 30, 0, 78, { (parent.mediaTime / parent.mediaDuration.toDouble()).toFloat().coerceIn(0f, 1f) }) {

    private val stream = Stream()

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if(!parent.blockEntity.isStreamInternal()) {
            super.renderButton(context, mouseX, mouseY, delta)
        }else{
            stream.renderButton(context, mouseX, mouseY, delta)
        }
        active = !parent.config && !parent.blockEntity.isStreamInternal()
        if(active && (isHovered || isDragged)) {
            val valueFromMouse = (mouseX - x) / (width - 2.0)
            val draggingMediaTime = (valueFromMouse * parent.mediaDuration).roundToLong()
            parent.playerTooltip.add(Text.translatable("screen.fabricvision.message.set_video_time", Text.literal(MediaPlayerScreen.formatTimestamp(draggingMediaTime)).styled { s -> s.withColor(0x00AFE4) }).formatted(Formatting.GRAY).asOrderedText())
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

    inner class Stream: PlayerSliderWidget(x, y, 166, 5, 30, 0, 82, { 1f }) {

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, 5, 30, width, height)
            context.drawTexture(MediaPlayerScreen.TEXTURE, x + 1, y + 1, 0, 82, width-2, 4)
            val textRenderer = MinecraftClient.getInstance().textRenderer
            val centerX = x + (166/2)
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            val configText = Text.translatable("screen.fabricvision.message.live")
            context.drawText(textRenderer, configText, (centerX * 2) - (textRenderer.getWidth(configText)/2), (y + 1)*2, 0xFFFFFF, false)
            context.matrices.pop()
        }
        override fun applyValue() = Unit

    }

}