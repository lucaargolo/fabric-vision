package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.client.render.screen.widget.*
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer.Status
import io.github.lucaargolo.fabricvision.utils.MathUtils
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.joml.Vector2i
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

//TODO: Fix this title
open class MediaPlayerScreen<M: MediaPlayerBlockEntity>(val blockEntity: M) : Screen(Text.translatable("media.player")) {

    private var age = 0

    private val backgroundWidth = 176
    private val backgroundHeight = 42

    private var x = 0
    private var y = 0

    var config = false
    protected val configDrawables = mutableListOf<Drawable>()

    protected var configBackgroundWidth = 50
    protected val configBackgroundHeight = 91

    protected var configX = 0
    protected var configY = 0

    var currentTime = System.currentTimeMillis()
    var startTime = blockEntity.startTime
    var mediaTime = ((currentTime - startTime)*blockEntity.rate.toDouble()).roundToLong()
    var mediaStatus = blockEntity.player?.status ?: Status.NO_PLAYER
    var mediaDuration = -1L
    var mediaTitle = ""

    var changedProgressCooldown = 0

    var playerTooltip = mutableListOf<OrderedText>()

    override fun shouldPause() = false

    override fun shouldCloseOnEsc(): Boolean {
        if(config) {
            config = false
            return false
        }
        return true
    }

    override fun clearChildren() {
        super.clearChildren()
        configDrawables.clear()
    }

    override fun init() {
        age = 0
        x = (width - backgroundWidth) / 2
        y = height - backgroundHeight
        configX = (width - configBackgroundWidth) / 2
        configY = (height - configBackgroundHeight) / 2
        addDrawableChild(EnableButtonWidget(this, x, y))
        addDrawableChild(ConfigButtonWidget(this, x+19, y+2))
        addDrawableChild(RepeatButtonWidget(this, x+34, y+2))
        addDrawableChild(NavigateButtonWidget(this, x+64, y+2, -5000L, 168))
        addDrawableChild(PlayButtonWidget(this, x+79, y))
        addDrawableChild(NavigateButtonWidget(this, x+98, y+2, 5000L, 182))
        addDrawableChild(VolumeSliderWidget(this, x+140, y+6))
        addDrawableChild(ProgressSliderWidget(this, x+5, y+30))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+12, 13, 164, 50, 165, { blockEntity.volume }, 0, { value ->
            Text.literal("Volume: ").formatted(VolumeSliderWidget.getFormatting(value)).append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+22, 13, 174, 50, 175, { blockEntity.light }, 1, { value ->
            Text.literal("Light: ").styled { s -> s.withColor(MathUtils.lerpColor(value, 0x555555, 0xFFFF00)) }.append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+32, 13, 184, 50, 185, { blockEntity.red }, 2, { value ->
            Text.literal("Red: ").styled { s -> s.withColor(MathUtils.lerpColor(value, 0x555555, 0xFF0000)) }.append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+42, 13, 194, 50, 195, { blockEntity.green }, 3, { value ->
            Text.literal("Green: ").styled { s -> s.withColor(MathUtils.lerpColor(value, 0x555555, 0x00FF00)) }.append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+52, 13, 204, 50, 205, { blockEntity.blue }, 4, { value ->
            Text.literal("Blue: ").styled { s -> s.withColor(MathUtils.lerpColor(value, 0x555555, 0x0000FF)) }.append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(ConfigSliderWidget(this, configX+13, configY+62, 13, 214, 50, 215, { blockEntity.alpha }, 5, { value ->
            Text.literal("Alpha: ").styled { s -> s.withColor(MathUtils.lerpColor(value, 0x555555, 0xFFFFFF)) }.append(Text.literal("${(value*100.0).roundToInt()}%").formatted(Formatting.GRAY))
        })))
        configDrawables.add(addSelectableChild(RateButtonWidget(this, configX+8, configY+76, 0.25f, 8)))
        configDrawables.add(addSelectableChild(RateButtonWidget(this, configX+15, configY+76, 0.50f, 15)))
        configDrawables.add(addSelectableChild(RateButtonWidget(this, configX+22, configY+76, 1.00f, 22)))
        configDrawables.add(addSelectableChild(RateButtonWidget(this, configX+29, configY+76, 1.50f, 29)))
        configDrawables.add(addSelectableChild(RateButtonWidget(this, configX+36, configY+76, 2.00f, 36)))
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
        renderProgressBar(context, mouseX, mouseY)
        super.render(context, mouseX, mouseY, delta)
        if(playerTooltip.isNotEmpty()) {
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            val biggestError = playerTooltip.sortedByDescending(textRenderer::getWidth).first()
            val errorWidth = textRenderer.getWidth(biggestError)
            val centerX = x + (backgroundWidth/2)
            context.drawTooltip(textRenderer, playerTooltip, { _, _, x, y, _, _ -> Vector2i(x, y) }, (centerX*2) - (errorWidth/2), (y - 10 - (max(0, playerTooltip.size-1)*4))*2)
            context.matrices.pop()
        }
        playerTooltip.clear()
        if(config) {
            val configCenterX = configX + (configBackgroundWidth/2)
            val rateCenterX = configX + (50/2)
            renderBackground(context)
            context.drawTexture(TEXTURE, configX, configY, 0, 152, configBackgroundWidth, configBackgroundHeight)
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)
            val configText = Text.literal("Config Screen")
            context.drawText(textRenderer, configText, (configCenterX * 2) - (textRenderer.getWidth(configText)/2), (configY + 5)*2, 0xFFFFFF, false)
            val rateText = Text.literal("Rate: ").formatted(Formatting.GRAY).append(Text.literal("${blockEntity.rate}x").styled { s -> s.withColor(0x00AFE4) })
            context.drawText(textRenderer, rateText, (rateCenterX * 2) - (textRenderer.getWidth(rateText)/2), (configY + 70)*2, 0xFFFFFF, false)
            context.matrices.pop()
            configDrawables.forEach {
                (it as? ClickableWidget)?.active = true
                it.render(context, mouseX, mouseY, delta)
            }
        }else{
            configDrawables.forEach {
                (it as? ClickableWidget)?.active = false
            }
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        children().forEach {
            (it as? ButtonWidget)?.isFocused = false
            (it as? PlayerSliderWidget)?.finishDragging()
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun renderProgressBar(context: DrawContext, mouseX: Int, mouseY: Int) {
        if (mediaDuration > 0L) {
            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)

            val showMediaTime = if (mediaTime > mediaDuration) mediaDuration else mediaTime
            val mediaTimeText = Text.literal(formatTimestamp(showMediaTime))
            context.drawText(textRenderer, mediaTimeText, (x + 6) * 2, (y + 25) * 2, 0xFFFFFF, false)

            val mediaDurationText = Text.literal(formatTimestamp(mediaDuration))
            context.drawText(textRenderer, mediaDurationText, (x + 170) * 2 - textRenderer.getWidth(mediaDurationText), (y + 25) * 2, 0xFFFFFF, false)

            val showMediaTitle = if (mediaTitle.length > 32) "..." + mediaTitle.substring(mediaTitle.length - 29, mediaTitle.length) else mediaTitle
            val mediaTitleText = Text.literal(showMediaTitle)
            val centerX = x + (backgroundWidth / 2)
            context.drawText(textRenderer, mediaTitleText, (centerX * 2) - (textRenderer.getWidth(mediaTitleText) / 2), (y + 25) * 2, 0xFFFFFF, false)

            context.matrices.pop()
        }
    }

    override fun tick() {
        age++
        mediaStatus = blockEntity.player?.status ?: Status.NO_PLAYER
        if (blockEntity.enabled) {
            currentTime = System.currentTimeMillis()
            startTime = blockEntity.startTime
            if(changedProgressCooldown > 0) {
                changedProgressCooldown--
            }else{
                mediaTime = ((currentTime - startTime)*blockEntity.rate.toDouble()).roundToLong()
            }
            if (blockEntity.repeating && mediaDuration > 0L) mediaTime %= mediaDuration
            blockEntity.player?.updateDuration {
                mediaDuration = it
            }
            blockEntity.player?.updateTitle {
                mediaTitle = it
            }
        }else{
            currentTime = 0L
            startTime = 0L
            mediaTime = 0L
            mediaDuration = 0L
            mediaTitle = ""
        }
        children().forEach {
            (it as? PlayerSliderWidget)?.tick()
            (it as? TextFieldWidget)?.tick()
        }
    }

    companion object {
        val TEXTURE = ModIdentifier("textures/gui/media_player.png")

        fun formatTimestamp(milliseconds: Long): String {
            val seconds = milliseconds / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val remainingSeconds = seconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        }
    }

}