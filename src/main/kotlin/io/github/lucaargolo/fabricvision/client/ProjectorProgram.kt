package io.github.lucaargolo.fabricvision.client

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.common.blockentity.ProjectorBlockEntity
import io.github.lucaargolo.fabricvision.compat.IrisCompat
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer
import ladysnake.satin.api.managed.ManagedShaderEffect
import ladysnake.satin.api.managed.ShaderEffectManager
import ladysnake.satin.api.util.GlMatrices
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.Camera
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

class ProjectorProgram {

    private var texture = TRANSPARENT.glId

    val framebuffer: SimpleFramebuffer by lazy {
        SimpleFramebuffer(854, 480, true, MinecraftClient.IS_SYSTEM_MAC)
    }

    val effect: ManagedShaderEffect by lazy {
        val client = MinecraftClient.getInstance()
        ShaderEffectManager.getInstance().manage(SHADER) { shader: ManagedShaderEffect ->
            shader.setSamplerUniform("MainSampler", client.framebuffer.colorAttachment)
            shader.setSamplerUniform("MainDepthSampler", (client.framebuffer as ReadableDepthFramebuffer).stillDepthMap)
            shader.setSamplerUniform("ProjectorSampler", texture)
            shader.setSamplerUniform("ProjectorDepthSampler", (framebuffer as ReadableDepthFramebuffer).stillDepthMap)
        }
    }

    private val mainInverseTransformMatrix = effect.findUniformMat4("MainInverseTransformMatrix")
    private val projectorTransformMatrix = effect.findUniformMat4("ProjectorTransformMatrix")

    private val cameraPosition = effect.findUniform3f("CameraPosition")
    private val projectorPosition = effect.findUniform3f("ProjectorPosition")

    private val viewPort = effect.findUniform2i("ViewPort")

    private val colorConfiguration = effect.findUniform4f("ColorConfiguration")
    private val projectionBrightness = effect.findUniform1f("ProjectionBrightness")
    private val projectionFallout = effect.findUniform1f("ProjectionFallout")

    fun updateTexture(player: MinecraftPlayer?, tickDelta: Float) {
        if(player != null) {
            val client = MinecraftClient.getInstance()
            val playerTexture = client.textureManager.getTexture(player.getTexture(tickDelta))
            if (playerTexture.glId != texture) {
                texture = playerTexture.glId
                effect.setSamplerUniform("ProjectorSampler", texture)
            }else{
                playerTexture.bindTexture()
                val i = IntArray(1)
                GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH, i)
                val width = i[0]
                GL11.glGetTexLevelParameteriv(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT, i)
                val height = i[0]
                if(framebuffer.textureWidth != width || framebuffer.textureHeight != height) {
                    framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
                }
            }
        }
    }

    fun updateConfiguration(client: MinecraftClient, camera: Camera, blockEntity: ProjectorBlockEntity) {
        colorConfiguration.set(blockEntity.red, blockEntity.green, blockEntity.blue, blockEntity.alpha)
        projectionBrightness.set(blockEntity.light)
        projectionFallout.set(blockEntity.fallout)
        viewPort.set(client.window.framebufferWidth, client.window.framebufferHeight)
        cameraPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
        mainInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat))
    }

    companion object {
        private val TRANSPARENT: AbstractTexture by lazy {
            MinecraftClient.getInstance().textureManager.getTexture(MinecraftPlayer.TRANSPARENT)
        }
        private val SHADER = ModIdentifier("shaders/post/projector.json")
        private val RENDER = linkedSetOf<ProjectorProgram>()
        private val QUEUE = linkedSetOf<ProjectorBlockEntity>()

        private val outMat = Matrix4f()

        fun queue(blockEntity: ProjectorBlockEntity) {
            QUEUE.add(blockEntity)
        }

        fun renderProjectors(camera: Camera, tickDelta: Float) {
            val client = MinecraftClient.getInstance()
            if(FabricVisionClient.isRenderingProjector) {
                val renderingProjector = FabricVisionClient.renderingProjector ?: return
                renderingProjector.projectorPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
                renderingProjector.projectorTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat).invert())
            }else {
                val iterator = QUEUE.iterator()
                while (iterator.hasNext()) {
                    val entity = iterator.next()
                    entity.projectorProgram?.updateTexture(entity.player, tickDelta)
                    entity.projectorProgram?.updateConfiguration(client, camera, entity)
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
                    iterator.remove()
                }
            }
        }

        private fun renderProjectorWorld(entity: ProjectorBlockEntity, tickDelta: Float, limitTime: Long, matrices: MatrixStack) {
            val projectorProgram = entity.projectorProgram ?: return
            val client = MinecraftClient.getInstance()
            val gameRenderer = client.gameRenderer
            val currentBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
            projectorProgram.framebuffer.beginWrite(true)
            FabricVisionClient.renderingProjector = projectorProgram
            if(FabricVisionClient.isRenderingProjector) {
                val backupRenderHand: Boolean = gameRenderer.renderHand
                gameRenderer.renderHand = false
                val backupViewDistance: Float = gameRenderer.viewDistance
                gameRenderer.viewDistance = 16f
                RenderSystem.backupProjectionMatrix()
                val backup = RenderSystem.getInverseViewRotationMatrix()
                IrisCompat.INSTANCE.setupProjectorWorldRender()
                val mat = RenderSystem.getModelViewStack()
                mat.push()
                mat.loadIdentity()
                RenderSystem.applyModelViewMatrix()
                gameRenderer.renderWorld(tickDelta, limitTime, matrices)
                mat.pop()
                RenderSystem.applyModelViewMatrix()
                IrisCompat.INSTANCE.endProjectorWorldRender(client.worldRenderer)
                RenderSystem.setInverseViewRotationMatrix(backup)
                RenderSystem.restoreProjectionMatrix()
                gameRenderer.viewDistance = backupViewDistance
                gameRenderer.renderHand = backupRenderHand
            }
            RENDER.add(projectorProgram)
            FabricVisionClient.renderingProjector = null
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, currentBuffer)
        }

        fun renderShaders(tickDelta: Float) {
            RENDER.forEach {
                it.effect.render(tickDelta)
            }
            RENDER.clear()
        }


    }

}