package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.blockentity.FlatScreenBlockEntity
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.Direction

class FlatScreenBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<FlatScreenBlockEntity> {

    override fun render(entity: FlatScreenBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        if(!entity.initialized) {
            entity.initialize()
        }

        val entry = matrices.peek()
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(entity.identifier))
        val red = 1f
        val green = 1f
        val blue = 1f
        val alpha = 1f
        val normal = Direction.NORTH.unitVector

        val x1 = 0.0f
        val x2 = 16.0f
        val y1 = 0.0f
        val y2 = 9.0f
        val z1 = 0.0f
        val z2 = 0.0f

        vertexConsumer?.vertex(entry.positionMatrix, x2, y1, z2)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, x2, y2, z2)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, x1, y2, z2)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, x1, y1, z2)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()


    }

}