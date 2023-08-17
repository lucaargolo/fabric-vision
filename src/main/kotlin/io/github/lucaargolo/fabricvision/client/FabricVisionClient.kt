package io.github.lucaargolo.fabricvision.client

import com.google.gson.JsonParser
import io.github.lucaargolo.fabricvision.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.fabricvision.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.fabricvision.client.render.item.DigitalCameraDynamicItemRenderer
import io.github.lucaargolo.fabricvision.client.render.item.DynamicItemRendererCompendium
import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.command.CommandCompendium
import io.github.lucaargolo.fabricvision.common.entity.EntityCompendium
import io.github.lucaargolo.fabricvision.common.fluid.FluidCompendium
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.common.recipe.RecipeSerializerCompendium
import io.github.lucaargolo.fabricvision.common.recipe.RecipeTypeCompendium
import io.github.lucaargolo.fabricvision.common.resource.ResourceCompendium
import io.github.lucaargolo.fabricvision.common.screenhandler.ScreenHandlerCompendium
import io.github.lucaargolo.fabricvision.common.sound.SoundCompendium
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.player.MinecraftAudioPlayerHolder
import io.github.lucaargolo.fabricvision.player.MinecraftImagePlayerHolder
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayerHolder
import io.github.lucaargolo.fabricvision.utils.ImgurHelper
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import io.github.lucaargolo.fabricvision.utils.ModLogger
import ladysnake.satin.api.event.PostWorldRenderCallbackV2
import ladysnake.satin.api.event.ShaderEffectRenderCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.text.Text
import net.minecraft.util.Util
import org.lwjgl.glfw.GLFW
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.roundToInt


object FabricVisionClient: ClientModInitializer {

    private val CAMERA_HUD = ModIdentifier("textures/gui/camera.png")

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

    var renderingProjector: ProjectorProgram? = null
    val isRenderingProjector
        get() = renderingProjector != null

    val isSneaking: Boolean
        get() {
            val client: MinecraftClient = MinecraftClient.getInstance()
            val handle: Long = client.window.handle
            val sneakKey: KeyBinding = client.options.sneakKey
            val boundKey: InputUtil.Key = sneakKey.boundKey
            var sneak = false
            if (boundKey.category == InputUtil.Type.MOUSE) {
                sneak = GLFW.glfwGetMouseButton(handle, boundKey.code) == 1
            } else if (boundKey.category == InputUtil.Type.KEYSYM) {
                sneak = GLFW.glfwGetKey(handle, boundKey.code) == 1
            }
            return sneak
        }

    private fun initializeRegistries() {
        BlockCompendium.initializeClient()
        ItemCompendium.initializeClient()
        FluidCompendium.initializeClient()
        BlockEntityCompendium.initializeClient()
        EntityCompendium.initializeClient()
        ResourceCompendium.initializeClient()
        PacketCompendium.initializeClient()
        RecipeSerializerCompendium.initializeClient()
        RecipeTypeCompendium.initializeClient()
        ScreenHandlerCompendium.initializeClient()
        CommandCompendium.initializeClient()
        SoundCompendium.initializeClient()
        BlockEntityRendererCompendium.initializeClient()
        EntityRendererCompendium.initializeClient()
        DynamicItemRendererCompendium.initializeClient()
    }
    override fun onInitializeClient() {
        initializeRegistries()
        ClientTickEvents.START_CLIENT_TICK.register { client ->
            MinecraftMediaPlayerHolder.clientTick(client)
            MinecraftAudioPlayerHolder.clientTick(client)
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
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            MinecraftMediaPlayerHolder.close(false)
            MinecraftAudioPlayerHolder.close()
            MinecraftImagePlayerHolder.close()
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            MinecraftMediaPlayerHolder.close(true)
            MinecraftAudioPlayerHolder.close()
            MinecraftImagePlayerHolder.close()
        }
        ShaderEffectRenderCallback.EVENT.register(ProjectorProgram::renderProjectors)
        PostWorldRenderCallbackV2.EVENT.register(ProjectorProgram::captureCameras)
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            out.accept(DigitalCameraDynamicItemRenderer.MODEL)
        }
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

        val zoom = ((1.0/digitalCameraFovMultiplier)*100.0).roundToInt()/100.0
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