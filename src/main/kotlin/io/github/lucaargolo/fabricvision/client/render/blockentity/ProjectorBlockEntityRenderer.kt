package io.github.lucaargolo.fabricvision.client.render.blockentity

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.client.FabricVisionClient
import io.github.lucaargolo.fabricvision.client.ProjectorProgram
import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.common.block.ProjectorBlock
import io.github.lucaargolo.fabricvision.common.blockentity.ProjectorBlockEntity
import io.github.lucaargolo.fabricvision.compat.IrisCompat
import io.github.lucaargolo.fabricvision.mixed.WorldRendererMixed
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.RotationPropertyHelper
import net.minecraft.util.math.Vec3d

class ProjectorBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<ProjectorBlockEntity> {

    override fun render(entity: ProjectorBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()

        matrices.push()

        val yaw = RotationPropertyHelper.toDegrees(entity.cachedState[ProjectorBlock.ROTATION]) +180f
        matrices.translate(0.5, 0.0, 0.5)
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw))
        matrices.translate(-0.5, 0.0, -0.5)
        val model = client.bakedModelManager.blockModels.getModel(BlockCompendium.PROJECTOR.defaultState)
        client.blockRenderManager.modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getEntitySolid(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), null, model, 1f, 1f, 1f, light, overlay)

        matrices.pop()

        if(MinecraftClient.isFabulousGraphicsOrBetter() || FabricVisionClient.isRenderingProjector || IrisCompat.INSTANCE.isRenderingShadowPass()) {
            return
        }

        entity.projectorProgram?.updateTexture(entity.player)
        val cameraEntityBackup = client.cameraEntity
        client.cameraEntity = entity.cameraEntity
        val nauseaIntensityBackup = client.player?.nauseaIntensity ?: 0f
        val prevNauseaIntensityBackup = client.player?.nauseaIntensity ?: 0f
        client.player?.nauseaIntensity = 0f
        client.player?.prevNauseaIntensity = 0f
        renderProjectorWorld(entity, tickDelta, 0L, MatrixStack())
        client.player?.nauseaIntensity = nauseaIntensityBackup
        client.player?.prevNauseaIntensity = prevNauseaIntensityBackup
        client.cameraEntity = cameraEntityBackup
        client.gameRenderer.camera.update(client.world, if (client.getCameraEntity() == null) client.player else client.getCameraEntity(), !client.options.perspective.isFirstPerson, client.options.perspective.isFrontView, tickDelta)
    }

    private fun renderProjectorWorld(entity: ProjectorBlockEntity, tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
        val projectorProgram = entity.projectorProgram ?: return
        val client = MinecraftClient.getInstance()
        val gameRenderer = client.gameRenderer
        projectorProgram.framebuffer.beginWrite(false)
        ProjectorProgram.setRendering(entity.projectorProgram)
        if(FabricVisionClient.isRenderingProjector) {
            val backupRenderHand: Boolean = gameRenderer.renderHand
            gameRenderer.renderHand = false
            val backupViewDistance: Float = gameRenderer.viewDistance
            gameRenderer.viewDistance = 16f
            RenderSystem.backupProjectionMatrix()
            val backup = RenderSystem.getInverseViewRotationMatrix()
            (client.worldRenderer as WorldRendererMixed).backup()
            client.worldRenderer.bufferBuilders = projectorProgram.bufferBuilders
            IrisCompat.INSTANCE.setupProjectorWorldRender()
            gameRenderer.renderWorld(tickDelta, limitTime, matrices)
            IrisCompat.INSTANCE.endProjectorWorldRender(client.worldRenderer)
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

    override fun rendersOutsideBoundingBox(blockEntity: ProjectorBlockEntity): Boolean {
        return true
    }

    override fun getRenderDistance(): Int {
        return 256
    }

    override fun isInRenderDistance(blockEntity: ProjectorBlockEntity, pos: Vec3d): Boolean {
        return Vec3d.ofCenter(blockEntity.pos).multiply(1.0, 0.0, 1.0).isInRange(pos.multiply(1.0, 0.0, 1.0), this.renderDistance.toDouble())
    }


}