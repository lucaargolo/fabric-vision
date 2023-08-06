package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

abstract class PlayerSliderWidget(x: Int, y: Int, width: Int, private val textureU: Int, private val textureV: Int, private val barU: Int, private val barV: Int, value: Float) : SliderWidget(x, y, width, 6, Text.empty(), value.toDouble()) {

    override fun updateMessage() = Unit

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(MediaPlayerScreen.TEXTURE, x, y, textureU, textureV, width, height)
        val buttonProgress = MathHelper.lerp(value.toFloat(), 0, width-6)
        val barProgress = MathHelper.lerp(value.toFloat(), 0, width-2)
        context.drawTexture(MediaPlayerScreen.TEXTURE, x + 1, y + 1, barU, barV, barProgress, 4)
        if(mouseX in (x+buttonProgress..x+buttonProgress+6) && mouseY in (y..y+6)) {
            context.drawTexture(MediaPlayerScreen.TEXTURE, x + buttonProgress, y, 0, 48, 6, 6)
        }else{
            context.drawTexture(MediaPlayerScreen.TEXTURE, x + buttonProgress, y, 0, 42, 6, 6)
        }
    }


}