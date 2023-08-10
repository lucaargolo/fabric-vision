package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.client.render.screen.widget.ConfigSliderWidget
import io.github.lucaargolo.fabricvision.client.render.screen.widget.ConfigValueFieldWidget
import io.github.lucaargolo.fabricvision.common.blockentity.HologramBlockEntity
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToInt

class HologramScreen(blockEntity: HologramBlockEntity) : MediaPlayerScreen<HologramBlockEntity>(blockEntity) {

    private fun reconfigureWidth() {
        configDrawables.forEach { drawable ->
            (drawable as? Widget)?.let {
                it.x -= configX
            }
        }
        configX = (width - configBackgroundWidth) / 2
        configDrawables.forEach { drawable ->
            (drawable as? Widget)?.let {
                it.x += configX
            }
        }
    }

    override fun init() {
        configBackgroundWidth = 152
        super.init()
        reconfigureWidth()
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+12, 65, 100, 168, 193, 169, { blockEntity.width }, 6, { value ->
            Text.translatable("screen.fabricvision.message.width", Text.literal("${value.toInt()}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, 0.0, 64.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+13, 18, 4, 13) { blockEntity.width }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+22, 65, 100, 178, 193, 179, { blockEntity.height }, 7, { value ->
            Text.translatable("screen.fabricvision.message.height", Text.literal("${value.toInt()}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, 0.0, 64.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+23, 18, 4, 14) { blockEntity.height }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+34, 65, 100, 190, 193, 191, { blockEntity.offsetX }, 8, { value ->
            Text.translatable("screen.fabricvision.message.x_offset", Text.literal("${(value*2).toInt()/2.0}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, -32.0, 32.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+35, 18, 4, 15) { blockEntity.offsetX }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+44, 65, 100, 200, 193, 201, { blockEntity.offsetY }, 9, { value ->
            Text.translatable("screen.fabricvision.message.y_offset", Text.literal("${(value*2).toInt()/2.0}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, -32.0, 32.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+45, 18, 4, 16) { blockEntity.offsetY }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+54, 65, 100, 210, 193, 211, { blockEntity.offsetZ }, 10, { value ->
            Text.translatable("screen.fabricvision.message.z_offset", Text.literal("${(value*2).toInt()/2.0}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, -32.0, 32.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+55, 18, 4, 17) { blockEntity.offsetZ }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+66, 65, 100, 223, 193, 224, { blockEntity.yaw/10f }, 11, { value ->
            Text.translatable("screen.fabricvision.message.yaw", Text.literal("${value.roundToInt()*10}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, -18.0, 18.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+68, 18, 4, 18) { blockEntity.yaw }))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+59, configY+76, 65, 100, 233, 193, 234, { blockEntity.pitch/10f }, 12, { value ->
            Text.translatable("screen.fabricvision.message.pitch", Text.literal("${value.roundToInt()*10}").formatted(Formatting.GRAY)).styled { s -> s.withColor(0x00AFE4) }
        }, -18.0, 18.0)))
        configDrawables.add(addSelectableChild(ConfigValueFieldWidget(this, textRenderer, configX+127, configY+78, 18, 4, 19) { blockEntity.pitch }))
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        configBackgroundWidth = 152
        super.render(context, mouseX, mouseY, delta)
    }

    override fun renderBackground(context: DrawContext) {
        super.renderBackground(context)
        context.drawTexture(TEXTURE, configX+46, configY, 87, 156, 106, configBackgroundHeight)
        configBackgroundWidth = 46
    }



}