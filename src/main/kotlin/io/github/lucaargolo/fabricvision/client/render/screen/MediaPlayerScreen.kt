package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.client.render.screen.widget.*
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer.Status
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper
import org.joml.Vector2i
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

//TODO: Fix this title
class MediaPlayerScreen(val blockEntity: MediaPlayerBlockEntity) : Screen(Text.translatable("media.player")) {

    private val backgroundWidth = 176
    private val backgroundHeight = 42

    private var x = 0
    private var y = 0

    var currentTime = System.currentTimeMillis()
    var startTime = blockEntity.startTime
    var mediaTime = currentTime - startTime
    var mediaStatus = blockEntity.player?.status ?: Status.NO_PLAYER
    var mediaDuration = -1L
    var mediaTitle = ""

    private var draggingProgress = false
    private var draggedCooldown = 0

    var playerTooltip = mutableListOf<OrderedText>()

    override fun shouldPause() = false

    override fun init() {
        x = (width - backgroundWidth) / 2
        y = height - backgroundHeight
        addDrawableChild(EnableButtonWidget(this, x, y))
        addDrawableChild(RepeatButtonWidget(this, x+34, y+2))
        addDrawableChild(NavigateButtonWidget(this, x+64, y+2, -5000L, 168))
        addDrawableChild(PlayButtonWidget(this, x+79, y))
        addDrawableChild(NavigateButtonWidget(this, x+98, y+2, 5000L, 182))
        addDrawableChild(VolumeSliderWidget(this, x+140, y+6, blockEntity.volume))
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
    }

    private fun getProgress(mouseX: Int, mouseY: Int, force: Boolean = false): Pair<Int, Int> {
        val (mediaProgress, buttonProgress) = getProgress(mouseX+0.0, mouseY+0.0, force)
        return mediaProgress.roundToInt() to buttonProgress.roundToInt()
    }

    private fun getProgress(mouseX: Double, mouseY: Double, force: Boolean = false): Pair<Double, Double> {
        val mediaProgress: Double
        val buttonProgress: Double
        if(!draggingProgress && !force) {
            val delta = mediaTime / (mediaDuration + 0.0)
            mediaProgress = MathHelper.lerp(delta.coerceIn(0.0, 1.0), 0.0, 164.0)
            buttonProgress = MathHelper.lerp(delta.coerceIn(0.0, 1.0), 0.0, 161.0)
        }else{
            mediaProgress = (mouseX - (x + 8.0)).coerceIn(0.0, 164.0)
            buttonProgress = (mouseX - (x + 8.0)).coerceIn(0.0, 161.0)
        }
        return mediaProgress to buttonProgress
    }

    private fun getDraggingMediaTime(mouseX: Int, mouseY: Int): Long {
        return getDraggingMediaTime(mouseX + 0.0, mouseY + 0.0)
    }

    private fun getDraggingMediaTime(mouseX: Double, mouseY: Double): Long {
        return if(mouseX in ((x+6.0)..(x+6.0+164.0)) && mouseY in (y+31.0)..(y+31.0+4.0)) {
            val (mediaProgress, _) = getProgress(mouseX, mouseY, true)
            val delta = mediaProgress/164.0
            (delta * mediaDuration).roundToLong()
        }else mediaTime
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(mouseX in ((x+6.0)..(x+6.0+164.0)) && mouseY in (y+31.0)..(y+31.0+4.0)) {
            draggingProgress = true
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if(draggingProgress) {
            draggingProgress = false
            val draggingMediaTime = getDraggingMediaTime(mouseX, mouseY)
            val difference = draggingMediaTime - mediaTime
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(blockEntity.pos)
            buf.writeLong(startTime - difference)
            ClientPlayNetworking.send(PacketCompendium.SET_TIME_BUTTON_C2S, buf)
            mediaTime = draggingMediaTime
            draggedCooldown = 20
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun renderProgressBar(context: DrawContext, mouseX: Int, mouseY: Int) {
        if(mediaDuration > 0L) {
            val (mediaProgress, buttonProgress) = getProgress(mouseX, mouseY)
            context.drawTexture(TEXTURE, x + 6, y + 31, 0, 78, mediaProgress, 4)
            if(draggingProgress || mouseX in ((x+5+buttonProgress)..(x+5+buttonProgress+6)) && mouseY in (y+30)..(y+30+6)) {
                context.drawTexture(TEXTURE, x + 5 + buttonProgress, y + 30, 0, 48, 6, 6)
            }else{
                context.drawTexture(TEXTURE, x + 5 + buttonProgress, y + 30, 0, 42, 6, 6)
            }

            context.matrices.push()
            context.matrices.scale(0.5f, 0.5f, 0.5f)

            val draggingMediaTime = getDraggingMediaTime(mouseX, mouseY)
            val showMediaTime = if(mediaTime > mediaDuration) mediaDuration else mediaTime
            if(draggingMediaTime != mediaTime) {
                playerTooltip.add(Text.literal("Set video time to ").formatted(Formatting.GRAY).append(Text.literal(formatTimestamp(draggingMediaTime)).styled { s -> s.withColor(0x00AFE4) }).asOrderedText())
            }
            val mediaTimeText = Text.literal(formatTimestamp(showMediaTime))
            context.drawText(textRenderer, mediaTimeText, (x + 6)*2, (y + 25)*2, 0xFFFFFF, false)


            val mediaDurationText = Text.literal(formatTimestamp(mediaDuration))
            context.drawText(textRenderer, mediaDurationText, (x + 170)*2 - textRenderer.getWidth(mediaDurationText), (y + 25)*2, 0xFFFFFF, false)

            val mediaTitleText = Text.literal(mediaTitle)
            val centerX = x + (backgroundWidth/2)
            context.drawText(textRenderer, mediaTitleText, (centerX * 2) - (textRenderer.getWidth(mediaDurationText))/2, (y + 25)*2, 0xFFFFFF, false)

            context.matrices.pop()
        }
    }

    private fun formatTimestamp(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    override fun tick() {
        currentTime = System.currentTimeMillis()
        startTime = blockEntity.startTime
        if(draggedCooldown > 0) {
            draggedCooldown--
        }else{
            mediaTime = currentTime - startTime
        }
        if (blockEntity.repeating) mediaTime %= mediaDuration
        mediaStatus = blockEntity.player?.status ?: Status.NO_PLAYER
        blockEntity.player?.updateDuration {
            mediaDuration = it
        }
        blockEntity.player?.updateTitle {
            mediaTitle = it
        }
    }

    companion object {
        val TEXTURE = ModIdentifier("textures/gui/media_player.png")
    }

}