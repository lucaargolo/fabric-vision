package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.block.PanelBlock
import io.github.lucaargolo.fabricvision.common.blockentity.PanelBlockEntity
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d

class PanelBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<PanelBlockEntity> {

    override fun render(entity: PanelBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        if(entity.activePanel == entity) {
            val identifier = entity.player?.identifier ?: MinecraftMediaPlayer.TRANSPARENT
            val renderLayer = RenderLayer.getEntityTranslucent(identifier)
            val vertexConsumer = vertexConsumers.getBuffer(renderLayer)

            val red = 1f
            val green = 1f
            val blue = 1f
            val alpha = 1f
            val normal = Direction.NORTH.unitVector

            val x = entity.currentXSize + 0f
            val y = entity.currentYSize + 0f

            val facing = entity.cachedState[PanelBlock.FACING]
            val rotation = when (facing) {
                Direction.EAST -> 90f
                Direction.SOUTH -> 180f
                Direction.WEST -> 270f
                else -> 0f
            }

            matrices.push()

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation))

            when(facing) {
                Direction.EAST -> matrices.translate(-1.0, 0.0, 2.05/16.0)
                Direction.SOUTH -> matrices.translate(-x + 0.0, 0.0, -2.05/16.0)
                Direction.WEST -> matrices.translate(0.0, 0.0, -13.95/16.0)
                else -> matrices.translate(-x + 1.0, 0.0, 13.95/16.0)
            }

            val entry = matrices.peek()

            if (facing.axis == Direction.Axis.X) {
                vertexConsumer?.vertex(entry.positionMatrix, x, 0f, 0f)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, x, y, 0f)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, 0f, y, 0f)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, 0f, 0f, 0f)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            } else {
                vertexConsumer?.vertex(entry.positionMatrix, x, 0f, 0f)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, x, y, 0f)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, 0f, y, 0f)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
                vertexConsumer?.vertex(entry.positionMatrix, 0f, 0f, 0f)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
            }

            matrices.pop()
        }

    }

    override fun rendersOutsideBoundingBox(blockEntity: PanelBlockEntity): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 256
    }

    override fun isInRenderDistance(blockEntity: PanelBlockEntity, pos: Vec3d): Boolean {
        return Vec3d.ofCenter(blockEntity.pos).multiply(1.0, 0.0, 1.0).isInRange(pos.multiply(1.0, 0.0, 1.0), this.renderDistance.toDouble())
    }

}