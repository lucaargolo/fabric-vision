package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock
import io.github.lucaargolo.fabricvision.common.blockentity.FlatScreenBlockEntity
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

class FlatScreenBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<FlatScreenBlockEntity> {

    override fun render(entity: FlatScreenBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        if(!entity.initialized) {
            entity.initialize()
        }

        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(entity.identifier))
        val red = 1f
        val green = 1f
        val blue = 1f
        val alpha = 1f
        val normal = Direction.NORTH.unitVector

        val x = 40.0f/16f
        val y = 22.5f/16f

        val facing = entity.cachedState[FlatScreenBlock.FACING]
        val rotation = when(facing) {
            Direction.EAST -> 90f
            Direction.SOUTH -> 180f
            Direction.WEST -> 270f
            else -> 0f
        }

        matrices.push()

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation))

        when(facing) {
            Direction.EAST -> matrices.translate(-28.0/16.0, 5.0/16.0, 8.55/16.0)
            Direction.SOUTH -> matrices.translate(-28.0/16.0, 5.0/16.0, -8.55/16.0)
            Direction.WEST -> matrices.translate(-12.0/16.0, 5.0/16.0, -7.45/16.0)
            else -> matrices.translate(-12.0/16.0, 5.0/16.0, 7.45/16.0)
        }

        val entry = matrices.peek()

        if(facing.axis == Direction.Axis.X) {
            vertexConsumer?.vertex(entry.positionMatrix, x, 0f, 0f)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, x, y, 0f)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, 0f, y, 0f)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, 0f, 0f, 0f)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        }else{
            vertexConsumer?.vertex(entry.positionMatrix, x, 0f, 0f)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, x, y, 0f)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, 0f, y, 0f)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            vertexConsumer?.vertex(entry.positionMatrix, 0f, 0f, 0f)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        }

        matrices.pop()

    }

}