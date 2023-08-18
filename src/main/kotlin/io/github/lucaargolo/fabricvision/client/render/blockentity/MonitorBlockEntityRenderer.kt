package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.block.HorizontalFacingMediaPlayerBlock
import io.github.lucaargolo.fabricvision.common.blockentity.MonitorBlockEntity
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.roundToInt

class MonitorBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<MonitorBlockEntity> {

    override fun render(entity: MonitorBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {

        val client = MinecraftClient.getInstance()
        val identifier = entity.player?.getTexture(tickDelta) ?: MinecraftPlayer.TRANSPARENT
        val texture = client.textureManager.getTexture(identifier)

        val renderLayer = RenderLayer.getEntityTranslucent(identifier)
        val vertexConsumer = vertexConsumers.getBuffer(renderLayer)

        val l = (entity.light * 15).roundToInt()
        val block = LightmapTextureManager.getBlockLightCoordinates(light)
        val sky = LightmapTextureManager.getSkyLightCoordinates(light)
        val lightmap = LightmapTextureManager.pack(max(l, block), max(l, sky))
        val red = entity.red
        val green = entity.green
        val blue = entity.blue
        val alpha = if(entity.enabled) entity.alpha else 0f
        val normal = Direction.UP.unitVector

        var x = 15.0f/16f
        var y = 8.44f/16f

        val facing = entity.cachedState[HorizontalFacingMediaPlayerBlock.FACING]
        val rotation = when(facing) {
            Direction.EAST -> 90f
            Direction.SOUTH -> 180f
            Direction.WEST -> 270f
            else -> 0f
        }

        matrices.push()

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation))

        if(identifier != MinecraftPlayer.TRANSPARENT) {
            texture.bindTexture()
            val i = IntArray(1)
            GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, i)
            val width = i[0] + 0f
            GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, i)
            val height = i[0] + 0f

            val screenAspectRatio = x / y
            val textureAspectRatio = width / height

            var xOffset = 0f
            var yOffset = 0f

            if (screenAspectRatio > textureAspectRatio) {
                xOffset = (x - (width * (y / height))) / 2
            } else {
                yOffset = (y - (height * (x / width))) / 2
            }

            x -= xOffset + xOffset
            y -= yOffset + yOffset
            matrices.translate(xOffset, yOffset, 0f)
        }

        when(facing) {
            Direction.EAST -> matrices.translate(-15.5/16.0, 5.0/16.0, 8.05/16.0)
            Direction.SOUTH -> matrices.translate(-15.5/16.0, 5.0/16.0, -8.05/16.0)
            Direction.WEST -> matrices.translate(0.5/16.0, 5.0/16.0, -7.95/16.0)
            else -> matrices.translate(0.5/16.0, 5.0/16.0, 7.95/16.0)
        }

        val entry = matrices.peek()

        if(facing.axis == Direction.Axis.X) {
            vertexConsumer.vertex(entry.positionMatrix, x, 0f, 0f).color(red, green, blue, alpha).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, x, y, 0f).color(red, green, blue, alpha).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, y, 0f).color(red, green, blue, alpha).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, 0f, 0f).color(red, green, blue, alpha).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        }else{
            vertexConsumer.vertex(entry.positionMatrix, x, 0f, 0f).color(red, green, blue, alpha).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, x, y, 0f).color(red, green, blue, alpha).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, y, 0f).color(red, green, blue, alpha).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
            vertexConsumer.vertex(entry.positionMatrix, 0f, 0f, 0f).color(red, green, blue, alpha).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(entry.normalMatrix, normal.x, normal.y, normal.z).next()
        }

        matrices.pop()

    }

}