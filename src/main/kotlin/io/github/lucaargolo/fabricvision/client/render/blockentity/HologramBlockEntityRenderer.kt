package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.blockentity.HologramBlockEntity
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer
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
import kotlin.math.roundToInt

class HologramBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<HologramBlockEntity> {

    override fun render(entity: HologramBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val identifier = entity.player?.getTexture(tickDelta) ?: MinecraftPlayer.TRANSPARENT
        val renderLayer = RenderLayer.getEntityTranslucent(identifier)
        val vertexConsumer = vertexConsumers.getBuffer(renderLayer)

        val l = (entity.light * 15).roundToInt()
        val lightmap = LightmapTextureManager.pack(l, l)
        val red = entity.red
        val green = entity.green
        val blue = entity.blue
        val alpha = if(entity.enabled) entity.alpha else 0f
        val normal = Direction.UP.unitVector

        val x = entity.width
        val y = entity.height

        matrices.push()

        matrices.translate(0.5, 1.0, 0.5)

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.yaw))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.pitch))

        matrices.translate(entity.offsetX, entity.offsetY, entity.offsetZ)

        val entry = matrices.peek()

        vertexConsumer.vertex(entry.positionMatrix, x, 0f, 0f).color(red, green, blue, alpha).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        vertexConsumer.vertex(entry.positionMatrix, x, y, 0f).color(red, green, blue, alpha).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        vertexConsumer.vertex(entry.positionMatrix, 0f, y, 0f).color(red, green, blue, alpha).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        vertexConsumer.vertex(entry.positionMatrix, 0f, 0f, 0f).color(red, green, blue, alpha).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()

        matrices.pop()

    }

    override fun rendersOutsideBoundingBox(blockEntity: HologramBlockEntity): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 256
    }

    override fun isInRenderDistance(blockEntity: HologramBlockEntity, pos: Vec3d): Boolean {
        return Vec3d.ofCenter(blockEntity.pos).multiply(1.0, 0.0, 1.0).isInRange(pos.multiply(1.0, 0.0, 1.0), this.renderDistance.toDouble())
    }

}