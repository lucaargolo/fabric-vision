package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToLong

class NavigateButtonWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int, private val time: Long, private val textureU: Int): ButtonWidget(x, y, 14, 14, Text.empty(), {  }, DEFAULT_NARRATION_SUPPLIER) {

    private val textureV: Int
        get() = if(active && (isHovered || isFocused)) 96 else 78

    private val realTime: Long
        get()  = if(FabricVisionClient.isSneaking) time*3 else time

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU + if(FabricVisionClient.isSneaking) 28 else 0, textureV, 14, 14)
        active = !parent.config
        if(active && isHovered) {
            if(realTime > 0) {
                parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.forward_video", Text.literal("${realTime/1000}s").styled { s -> s.withColor(0x00AFE4) }).formatted(Formatting.GRAY).asOrderedText())
            }else{
                parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.backward_video", Text.literal("${realTime/-1000}s").styled { s -> s.withColor(0x00AFE4) }).formatted(Formatting.GRAY).asOrderedText())
            }
        }
    }

    override fun onPress() {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(parent.blockEntity.pos)
        val n = parent.mediaTime + (realTime/parent.blockEntity.rate.toDouble()).roundToLong()
        val t = when {
            n <= 0 -> -parent.mediaTime
            !parent.blockEntity.repeating && n >= parent.mediaDuration -> {
                parent.mediaDuration - parent.mediaTime
            }
            else -> (realTime/parent.blockEntity.rate.toDouble()).roundToLong()
        }
        buf.writeLong(parent.startTime - t)
        ClientPlayNetworking.send(PacketCompendium.SET_TIME_BUTTON_C2S, buf)
    }

}