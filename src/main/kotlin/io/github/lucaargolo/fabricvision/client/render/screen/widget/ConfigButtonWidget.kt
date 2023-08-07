package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer.Status
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ConfigButtonWidget(private val parent: MediaPlayerScreen, x: Int, y: Int): ButtonWidget(x, y, 14, 14, Text.empty(), {  }, DEFAULT_NARRATION_SUPPLIER) {

        private val textureU: Int
            get() = 168


        private val textureV: Int
            get() = if(active && hovered) 60 else 42

        override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU, textureV, 14, 14)
            active = !parent.config
            if(active && isHovered) {
                //TODO: Also translate this
                parent.playerTooltip.add(Text.literal("Open config screen").formatted(Formatting.GRAY).asOrderedText())
            }
        }

        override fun onPress() {
            parent.config = !parent.config
        }

    }