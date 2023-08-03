package io.github.lucaargolo.fabricvision.client.render.blockentity

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.client.ProjectorProgram
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.mixed.WorldRendererMixed
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

class ProjectorBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<MediaPlayerBlockEntity.Projector> {

    //TODO: Disable projector on fabulous.
    override fun render(entity: MediaPlayerBlockEntity.Projector, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()

        if(FabricVisionClient.isRenderingProjector) {
            return
        }

        entity.projectorProgram?.updateTexture(entity.player)
        val cameraEntityBackup = client.cameraEntity
        client.cameraEntity = entity.cameraEntity
        renderProjectorWorld(entity, tickDelta, 0L, MatrixStack())
        client.cameraEntity = cameraEntityBackup
        client.gameRenderer.camera.update(client.world, if (client.getCameraEntity() == null) client.player else client.getCameraEntity(), !client.options.perspective.isFirstPerson, client.options.perspective.isFrontView, tickDelta)
    }

    private fun renderProjectorWorld(entity: MediaPlayerBlockEntity.Projector, tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
        val client = MinecraftClient.getInstance()
        val gameRenderer = client.gameRenderer
        ProjectorProgram.setRendering(entity.projectorProgram)
        FabricVisionClient.projectorFramebuffer?.beginWrite(true)
        if(FabricVisionClient.isRenderingProjector) {
            val backupRenderHand: Boolean = gameRenderer.renderHand
            gameRenderer.renderHand = false
            val backupViewDistance: Float = gameRenderer.viewDistance
            gameRenderer.viewDistance = 16f
            RenderSystem.backupProjectionMatrix()
            val backup = RenderSystem.getInverseViewRotationMatrix()
            (client.worldRenderer as WorldRendererMixed).backup()
            client.worldRenderer.bufferBuilders = FabricVisionClient.projectorBufferBuilders
            gameRenderer.renderWorld(tickDelta, limitTime, matrices)
            client.worldRenderer.bufferBuilders = client.bufferBuilders
            (client.worldRenderer as WorldRendererMixed).restore()
            RenderSystem.setInverseViewRotationMatrix(backup)
            RenderSystem.restoreProjectionMatrix()
            gameRenderer.viewDistance = backupViewDistance
            gameRenderer.renderHand = backupRenderHand
        }
        ProjectorProgram.setRendering(null)
        client.framebuffer.beginWrite(true)
    }

    override fun rendersOutsideBoundingBox(blockEntity: MediaPlayerBlockEntity.Projector): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 256
    }

    override fun isInRenderDistance(blockEntity: MediaPlayerBlockEntity.Projector, pos: Vec3d): Boolean {
        return Vec3d.ofCenter(blockEntity.pos).multiply(1.0, 0.0, 1.0).isInRange(pos.multiply(1.0, 0.0, 1.0), this.renderDistance.toDouble())
    }


}