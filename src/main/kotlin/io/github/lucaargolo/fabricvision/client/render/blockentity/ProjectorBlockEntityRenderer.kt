package io.github.lucaargolo.fabricvision.client.render.blockentity

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.common.block.FlatScreenBlock
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.mixed.WorldRendererMixed
import ladysnake.satin.api.util.GlMatrices
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

class ProjectorBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<MediaPlayerBlockEntity.Projector> {

    //TODO: Disable projector on fabulous.
    override fun render(entity: MediaPlayerBlockEntity.Projector, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()

        if(FabricVisionClient.isRenderingProjector) {
            return
        }

        val cameraEntityBackup = client.cameraEntity

        val cameraLastYBackup = client.gameRenderer.camera.lastCameraY
        val cameraYBackup = client.gameRenderer.camera.cameraY

        client.cameraEntity = entity.cameraEntity
        renderProjectorWorld(tickDelta, 0L, MatrixStack())
        client.cameraEntity = cameraEntityBackup
        client.gameRenderer.camera.update(client.world, if (client.getCameraEntity() == null) client.player else client.getCameraEntity(), !client.options.perspective.isFirstPerson, client.options.perspective.isFrontView, tickDelta)

        client.gameRenderer.camera.lastCameraY = cameraLastYBackup
        client.gameRenderer.camera.cameraY = cameraYBackup

        return
        //val identifier = entity.player?.identifier ?: MinecraftMediaPlayer.TRANSPARENT
        val renderLayer = RenderLayer.getEntityTranslucent(FabricVisionClient.colorTexture)
        val vertexConsumer = vertexConsumers.getBuffer(renderLayer)

        val red = 1f
        val green = 1f
        val blue = 1f
        val alpha = 1f
        val normal = Direction.NORTH.unitVector

        val x = 16/2.0f
        val y = 9/2.0f

        val facing = entity.cachedState[FlatScreenBlock.FACING]
        val rotation = when(facing) {
            Direction.EAST -> 90f
            Direction.SOUTH -> 180f
            Direction.WEST -> 270f
            else -> 0f
        }

        matrices.push()

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation))

//        when(facing) {
//            Direction.EAST -> matrices.translate(-28.0/16.0, 5.0/16.0, 8.55/16.0)
//            Direction.SOUTH -> matrices.translate(-28.0/16.0, 5.0/16.0, -8.55/16.0)
//            Direction.WEST -> matrices.translate(-12.0/16.0, 5.0/16.0, -7.45/16.0)
//            else -> matrices.translate(-12.0/16.0, 5.0/16.0, 7.45/16.0)
//        }

        val entry = matrices.peek()

        vertexConsumer?.vertex(entry.positionMatrix, x, 0f, 0f)?.color(red, green, blue, alpha)?.texture(0f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, x, y, 0f)?.color(red, green, blue, alpha)?.texture(0f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, 0f, y, 0f)?.color(red, green, blue, alpha)?.texture(1f, 1f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()
        vertexConsumer?.vertex(entry.positionMatrix, 0f, 0f, 0f)?.color(red, green, blue, alpha)?.texture(1f, 0f)?.overlay(OverlayTexture.DEFAULT_UV)?.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)?.normal(entry.normalMatrix, normal.x, normal.y, normal.z)?.next()

        matrices.pop()

    }

    //TODO: Cancel nausea
    private fun renderProjectorWorld(tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
        val client = MinecraftClient.getInstance()
        val gameRenderer = client.gameRenderer
        FabricVisionClient.isRenderingProjector = true
        FabricVisionClient.projectorFramebuffer.beginWrite(true)
        val backupRenderHand: Boolean = gameRenderer.renderHand
        gameRenderer.renderHand = false
        val backupViewDistance: Float = gameRenderer.viewDistance
        gameRenderer.viewDistance = 16f
        RenderSystem.backupProjectionMatrix()
        val backup = RenderSystem.getInverseViewRotationMatrix()
        (client.worldRenderer as WorldRendererMixed).backup()
        client.worldRenderer.bufferBuilders = FabricVisionClient.projectorBufferBuilders
        gameRenderer.renderWorld(tickDelta, limitTime, matrices)
        (client.worldRenderer as WorldRendererMixed).restore()
        RenderSystem.setInverseViewRotationMatrix(backup)
        RenderSystem.restoreProjectionMatrix()
        gameRenderer.viewDistance = backupViewDistance
        gameRenderer.renderHand = backupRenderHand
        FabricVisionClient.isRenderingProjector = false
        client.framebuffer.beginWrite(true)
    }

}