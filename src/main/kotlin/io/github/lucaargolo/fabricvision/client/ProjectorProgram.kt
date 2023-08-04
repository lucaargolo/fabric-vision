package io.github.lucaargolo.fabricvision.client

import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer
import ladysnake.satin.api.managed.ManagedShaderEffect
import ladysnake.satin.api.managed.ShaderEffectManager
import ladysnake.satin.api.util.GlMatrices
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.BufferBuilderStorage
import net.minecraft.client.render.Camera
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f

class ProjectorProgram {

    private var texture = MinecraftClient.getInstance().textureManager.getTexture(MinecraftMediaPlayer.TRANSPARENT).glId

    val framebuffer: SimpleFramebuffer by lazy {
        val client = MinecraftClient.getInstance()
        SimpleFramebuffer(client.window.framebufferWidth, client.window.framebufferHeight, true, MinecraftClient.IS_SYSTEM_MAC)
    }

    var bufferBuilders = BufferBuilderStorage()

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

    private val viewPort = effect.findUniform4i("ViewPort")

    private val projectionFallout = effect.findUniform1f("ProjectionFallout")

    fun updateTexture(player: MinecraftMediaPlayer?) {
        val playerTexture = player?.texture
        if(playerTexture != null && playerTexture.glId != texture) {
            texture = playerTexture.glId
            effect.setSamplerUniform("ProjectorSampler", texture)
        }
    }

    companion object {
        private val SHADER = ModIdentifier("shaders/post/projector.json")
        private val RENDER = linkedSetOf<ProjectorProgram>()

        private val outMat = Matrix4f()

        fun setRendering(projector: ProjectorProgram?) {
            FabricVisionClient.renderingProjector = projector
            projector?.let {
                RENDER.add(it)
                val client = MinecraftClient.getInstance()
                if(it.framebuffer.viewportWidth != client.window.framebufferWidth || it.framebuffer.viewportHeight != client.window.framebufferHeight) {
                    it.framebuffer.resize(client.window.framebufferWidth, client.window.framebufferHeight, MinecraftClient.IS_SYSTEM_MAC)
                }
            }
        }

        fun renderProjectors(tickDelta: Float) {
            RENDER.forEach {
                it.effect.render(tickDelta)
            }
            RENDER.clear()
        }

        fun captureCameras(matrices: MatrixStack, camera: Camera, tickDelta: Float, nanoTime: Long) {
            val client = MinecraftClient.getInstance()
            RENDER.forEach {
                if (!FabricVisionClient.isRenderingProjector) {
                    it.viewPort.set(0, 0, client.window.framebufferWidth + 0, client.window.framebufferHeight + 0)
                    it.cameraPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
                    it.mainInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat))
                }else if(FabricVisionClient.renderingProjector == it) {
                    it.projectorPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
                    it.projectorTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat).invert())
                }
            }
        }

    }

}