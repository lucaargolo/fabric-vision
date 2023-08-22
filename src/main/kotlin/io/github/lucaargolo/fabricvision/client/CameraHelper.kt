package io.github.lucaargolo.fabricvision.client

import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.common.sound.SoundCompendium
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.utils.ImgurHelper
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import io.github.lucaargolo.fabricvision.utils.ModLogger
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Util
import org.lwjgl.opengl.GL11
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.roundToInt

object CameraHelper {

    private val CAMERA_HUD = ModIdentifier("textures/gui/camera.png")

    val cameraFramebuffer = SimpleFramebuffer(854, 480, false, MinecraftClient.IS_SYSTEM_MAC)
    var renderedCamera = false

    var hudHiddenBackup = false
    var takePicture = 0

    var takingPicture = false
    var errorTakingPicture = 0
    var takenPicture = 0

    var uploadingPicture = false
    var errorUploadingPicture = 0
    var uploadedPicture = 0

    var errorSavingPicture = 0
    var savedPicture = 0

    var digitalCameraFovMultiplier = 1f

    @JvmStatic
    fun PlayerEntity.isUsingCamera(): Boolean {
        return (this.isUsingItem && this.activeItem.isOf(ItemCompendium.DIGITAL_CAMERA)) || (world.isClient && (takePicture > 0 || takingPicture || takenPicture > 90 || uploadingPicture || uploadedPicture > 90))
    }
    
    fun clientTick(client: MinecraftClient) {
        if(takePicture > 0 && --takePicture == 0) {
            client.options.hudHidden = hudHiddenBackup
            takeCameraPicture()
        }
        if(takenPicture > 0) takenPicture--
        if(errorTakingPicture > 0) errorTakingPicture--
        if(errorUploadingPicture > 0) errorUploadingPicture--
        if(uploadedPicture > 0) uploadedPicture--
        if(errorSavingPicture > 0) errorSavingPicture--
        if(savedPicture > 0) savedPicture--
    }

    fun updateCameraFramebuffer() {
        if(renderedCamera) {
            val client = MinecraftClient.getInstance()
            val framebuffer = client.framebuffer
            if (framebuffer.textureWidth != cameraFramebuffer.textureWidth || framebuffer.textureHeight != cameraFramebuffer.textureHeight) {
                cameraFramebuffer.resize(framebuffer.textureWidth, framebuffer.textureHeight, MinecraftClient.IS_SYSTEM_MAC)
            }
            cameraFramebuffer.copyColorFrom(framebuffer)
            framebuffer.beginWrite(true)
            renderedCamera = false
        }
    }

    fun Framebuffer.copyColorFrom(framebuffer: Framebuffer) {
        RenderSystem.assertOnRenderThreadOrInit()
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, framebuffer.fbo)
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, fbo)
        GlStateManager._glBlitFrameBuffer(0, 0, framebuffer.textureWidth, framebuffer.textureHeight, 0, 0, textureWidth, textureHeight, GL11.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST)
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0)
    }

    fun renderDigitalCameraHud(context: DrawContext, textRenderer: TextRenderer, scaledWidth: Int, scaledHeight: Int, scale: Float) {
        if(takingPicture) {
            val string = Text.translatable("screen.fabricvision.taking_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 15, 0xFFFFFF, true)
        }else if(errorSavingPicture > 0) {
            val string = Text.translatable("screen.fabricvision.no_blank_discs")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 15, 0xFFFF00, true)
        }else if(savedPicture > 0) {
            val string = Text.translatable("screen.fabricvision.saved_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 15, 0x00FF00, true)
        }else if(takenPicture > 0) {
            val string = Text.translatable("screen.fabricvision.taken_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 15, 0x00FF00, true)
        }else if(errorTakingPicture > 0) {
            val string = Text.translatable("screen.fabricvision.error_taking_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 15, 0xFF0000, true)
        }

        if(uploadingPicture) {
            val string = Text.translatable("screen.fabricvision.uploading_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 25, 0xFFFFFF, true)
        }else if(uploadedPicture > 0) {
            val string = Text.translatable("screen.fabricvision.uploaded_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 25, 0x00FF00, true)
        }else if(errorUploadingPicture > 0) {
            val string = Text.translatable("screen.fabricvision.error_uploading_picture")
            val x = (scaledWidth - textRenderer.getWidth(string)) / 2
            context.drawText(textRenderer, string, x, 25, 0xFF0000, true)
        }

        val client = MinecraftClient.getInstance()

        val zoom = ((1.0/ digitalCameraFovMultiplier)*100.0).roundToInt()/100.0
        val string1 = Text.translatable("screen.fabricvision.key_to_take_picture", client.options.attackKey.boundKeyLocalizedText)
        val string2 = Text.translatable("screen.fabricvision.zoom", zoom)
        val x1 = (scaledWidth - textRenderer.getWidth(string1)) / 2
        val x2 = (scaledWidth - textRenderer.getWidth(string2)) / 2
        context.drawText(textRenderer, string1, x1, scaledHeight - 25, 0xFFFFFF, true)
        context.drawText(textRenderer, string2, x2, scaledHeight - 35, 0xFFFFFF, true)

        context.drawTexture(CAMERA_HUD, 15, 15, 0, 0, 64, 64)
        context.drawTexture(CAMERA_HUD, scaledWidth-64-15, 15, 192, 0, 64, 64)
        context.drawTexture(CAMERA_HUD, 15, scaledHeight-64-15, 0, 192, 64, 64)
        context.drawTexture(CAMERA_HUD, scaledWidth-64-15, scaledHeight-64-15, 192, 192, 64, 64)
    }

    fun takeCameraPicture() {
        if(takingPicture) return
        takingPicture = true

        val client = MinecraftClient.getInstance()
        client.soundManager.play(PositionedSoundInstance.master(SoundCompendium.CAMERA, 1.0f))

        val nativeImage = ScreenshotRecorder.takeScreenshot(client.framebuffer)
        val directory = File(client.runDirectory, "screenshots${File.separator}camera").also(File::mkdirs)
        val screenshot = getScreenshotFilename(directory)

        Util.getIoWorkerExecutor().execute {
            val success = try {
                nativeImage.writeTo(screenshot)
                takenPicture = 100
                true
            } catch (e: Exception) {
                ModLogger.warn("Couldn't save camera picture", e)
                errorTakingPicture = 100
                false
            } finally {
                nativeImage.close()
                takingPicture = false
            }

            if(success) {
                thread {
                    uploadingPicture = true
                    try {
                        val result = ImgurHelper.upload(screenshot)
                        Util.getIoWorkerExecutor().execute {
                            try{
                                val resultFile = File(screenshot.absolutePath.replace("png", "json"))
                                if(resultFile.createNewFile()) {
                                    resultFile.writeText(result)
                                }
                            }catch (ignored: Exception) {

                            }
                        }
                        val json = JsonParser.parseString(result).asJsonObject
                        val data = json.getAsJsonObject("data")
                        val link = data.getAsJsonPrimitive("link").asString
                        val delete = data.getAsJsonPrimitive("deletehash").asString

                        val buf = PacketByteBufs.create()
                        buf.writeString(link)
                        buf.writeString(delete)
                        ClientPlayNetworking.send(PacketCompendium.SEND_CAMERA_PIC_C2S, buf)
                        uploadedPicture = 100
                    }catch (e: Exception) {
                        ModLogger.warn("Couldn't upload picture to Imgur", e)
                        errorUploadingPicture = 100
                    }
                    uploadingPicture = false
                }
            }
        }
    }

    private fun getScreenshotFilename(directory: File): File {
        val string = Util.getFormattedCurrentTime()
        var i = 1
        while (true) {
            val file = File(directory, string + (if (i == 1) "" else "_$i") + ".png")
            if (!file.exists()) {
                return file
            }
            ++i
        }
    }
    
}