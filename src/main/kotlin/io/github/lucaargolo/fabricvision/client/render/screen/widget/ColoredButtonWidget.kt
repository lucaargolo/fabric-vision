package io.github.lucaargolo.fabricvision.client.render.screen.widget

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ColoredButtonWidget private constructor(x: Int, y: Int, width: Int, height: Int, var color: Int, message: Text, onPress: PressAction, narrationSupplier: NarrationSupplier) : ButtonWidget(x, y, width, height, message, onPress, narrationSupplier) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val minecraftClient = MinecraftClient.getInstance()
        val red = ((color shr 16) and 0xFF) / 255f
        val green = ((color shr 8) and 0xFF) / 255f
        val blue = ((color shr 0) and 0xFF) / 255f
        context.setShaderColor(red, green, blue, alpha)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        context.drawNineSlicedTexture(WIDGETS_TEXTURE, x, y, getWidth(), getHeight(), 20, 4, 200, 20, 0, getTextureY())
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        val i = if (active) 16777215 else 10526880
        drawMessage(context, minecraftClient.textRenderer, i or (MathHelper.ceil(alpha * 255.0f) shl 24))
    }

    private fun getTextureY(): Int {
        var i = 1
        if (!active) {
            i = 0
        } else if (this.isSelected) {
            i = 2
        }
        return 46 + i * 20
    }

    class Builder(private val message: Text, private val onPress: PressAction) {
        private var tooltip: Tooltip? = null
        private var x = 0
        private var y = 0
        private var width = 150
        private var height = 20
        private var narrationSupplier: NarrationSupplier
        private var color = 0xFFFFFF

        init {
            narrationSupplier = DEFAULT_NARRATION_SUPPLIER
        }

        fun color(color: Int): Builder {
            this.color = color
            return this
        }

        fun position(x: Int, y: Int): Builder {
            this.x = x
            this.y = y
            return this
        }

        fun width(width: Int): Builder {
            this.width = width
            return this
        }

        fun size(width: Int, height: Int): Builder {
            this.width = width
            this.height = height
            return this
        }

        fun dimensions(x: Int, y: Int, width: Int, height: Int): Builder {
            return position(x, y).size(width, height)
        }

        fun tooltip(tooltip: Tooltip?): Builder {
            this.tooltip = tooltip
            return this
        }

        fun narrationSupplier(narrationSupplier: NarrationSupplier): Builder {
            this.narrationSupplier = narrationSupplier
            return this
        }

        fun build(): ColoredButtonWidget {
            val buttonWidget = ColoredButtonWidget(x, y, width, height, color, message, onPress, narrationSupplier)
            buttonWidget.tooltip = tooltip
            return buttonWidget
        }
    }

    companion object {
        @JvmStatic
        fun builder(message: Text, onPress: PressAction): Builder {
            return Builder(message, onPress)
        }
    }


}