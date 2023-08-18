package io.github.lucaargolo.fabricvision.client.render.item

import io.github.lucaargolo.fabricvision.client.FramebufferTexture
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Arm
import net.minecraft.util.Identifier

class DigitalCameraDynamicItemRenderer: DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val leftHanded = client.options.mainArm.value == Arm.LEFT
        val model = client.bakedModelManager.getModel(MODEL)
        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        client.itemRenderer.renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model)
        val player = client.player ?: return
        if(player.mainHandStack == stack || player.offHandStack == stack) {
            if (client.textureManager.getOrDefault(TEXTURE, null) == null) {
                val framebufferTexture = FramebufferTexture(client.framebuffer, false)
                client.textureManager.registerTexture(TEXTURE, framebufferTexture)
            }

            val renderLayer = RenderLayer.getEntityTranslucent(TEXTURE)
            val vertexConsumer = vertexConsumers.getBuffer(renderLayer)

            matrices.translate(-3.1 / 16.0, -7 / 16.0, 1.26 / 16.0)

            val entry = matrices.peek()

            vertexConsumer.vertex(entry.positionMatrix, 5 / 16f, 0f, 0f).color(1f, 1f, 1f, 1f).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.normalMatrix, 0f, 1f, 0f).next()
            vertexConsumer.vertex(entry.positionMatrix, 5 / 16f, 3 / 16f, 0f).color(1f, 1f, 1f, 1f).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.normalMatrix, 0f, 1f, 0f).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, 3 / 16f, 0f).color(1f, 1f, 1f, 1f).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.normalMatrix, 0f, 1f, 0f).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, 0f, 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(entry.normalMatrix, 0f, 1f, 0f).next()
        }
        matrices.pop()
    }

    companion object {
        private val TEXTURE: Identifier = ModIdentifier("camera_framebuffer")
        val MODEL = ModelIdentifier(ModIdentifier("digital_camera_model"), "inventory")
    }

}