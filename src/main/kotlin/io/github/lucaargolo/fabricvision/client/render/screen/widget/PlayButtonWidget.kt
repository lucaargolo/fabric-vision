package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer.Status
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class PlayButtonWidget(private val parent: MediaPlayerScreen<*>, x: Int, y: Int): ButtonWidget(x, y, 18, 18, Text.empty(), {  }, DEFAULT_NARRATION_SUPPLIER) {

    private val textureU: Int
        get() = when {
            parent.mediaStatus == Status.NO_MEDIA -> 96
            FabricVisionClient.isSneaking && parent.mediaStatus.interactable -> 42
            !FabricVisionClient.isSneaking && parent.mediaStatus == Status.PLAYING -> 24
            parent.mediaStatus == Status.PAUSED || parent.mediaStatus == Status.STOPPED -> 6
            else -> 114
        }

    private val textureV: Int
        get() = if(active && (isHovered || isFocused)) 60 else 42

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU, textureV, 18, 18)
        active = !parent.config
        if(active && isHovered) {
            if (!parent.mediaStatus.interactable) {
                parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.status").append(": ").formatted(parent.mediaStatus.formatting).append(Text.translatable(parent.mediaStatus.translationKey).formatted(Formatting.GRAY)).asOrderedText())
                parent.playerTooltip.add(Text.translatable(parent.mediaStatus.descriptionKey).formatted(Formatting.GRAY).asOrderedText())
            }else{
                when(textureU) {
                    6 -> parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.play").styled { s -> s.withColor(0x00AFE4) }.asOrderedText())
                    24 -> parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.pause").styled { s -> s.withColor(0x00AFE4) }.asOrderedText())
                    42 -> parent.playerTooltip.add(Text.translatable("tooltip.fabricvision.stop").styled { s -> s.withColor(0x00AFE4) }.asOrderedText())
                }
            }
        }
    }

    override fun onPress() {
        if(parent.mediaStatus.interactable) {
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(parent.blockEntity.pos)
            val play = FabricVisionClient.isSneaking || parent.mediaStatus == Status.STOPPED
            val stop = FabricVisionClient.isSneaking
            buf.writeBoolean(play)
            buf.writeBoolean(stop)
            ClientPlayNetworking.send(PacketCompendium.PLAY_BUTTON_C2S, buf)
        }
    }

}