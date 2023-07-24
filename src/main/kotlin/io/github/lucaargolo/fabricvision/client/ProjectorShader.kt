package io.github.lucaargolo.fabricvision.client

import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import ladysnake.satin.api.event.PostWorldRenderCallbackV2
import ladysnake.satin.api.event.ShaderEffectRenderCallback
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer
import ladysnake.satin.api.managed.ManagedShaderEffect
import ladysnake.satin.api.managed.ShaderEffectManager
import ladysnake.satin.api.util.GlMatrices
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

object ProjectorShader: PostWorldRenderCallbackV2, ShaderEffectRenderCallback {

    private val PROJECTOR_SHADER = ModIdentifier("shaders/post/projector.json")

    val projectorShader: ManagedShaderEffect by lazy {
        val client = MinecraftClient.getInstance()
        ShaderEffectManager.getInstance().manage(PROJECTOR_SHADER) { shader: ManagedShaderEffect ->
            shader.setSamplerUniform("MainSampler", client.framebuffer.colorAttachment)
            shader.setSamplerUniform("MainDepthSampler", (client.framebuffer as ReadableDepthFramebuffer).stillDepthMap)
            shader.setSamplerUniform("ProjectorSampler", client.textureManager.getTexture(ModIdentifier("textures/gui/img.png")).glId)
            shader.setSamplerUniform("ProjectorDepthSampler", (FabricVisionClient.projectorFramebuffer as ReadableDepthFramebuffer).stillDepthMap)
        }
    }

    private val mainTransformMatrix = projectorShader.findUniformMat4("MainTransformMatrix")
    private val mainInverseTransformMatrix = projectorShader.findUniformMat4("MainInverseTransformMatrix")
    private val projectorTransformMatrix = projectorShader.findUniformMat4("ProjectorTransformMatrix")
    private val projectorInverseTransformMatrix = projectorShader.findUniformMat4("ProjectorInverseTransformMatrix")

    private val cameraPosition = projectorShader.findUniform3f("CameraPosition")
    private val projectorPosition = projectorShader.findUniform3f("ProjectorPosition")

    private val viewPort = projectorShader.findUniform4i("ViewPort")

    private val nearPlane = projectorShader.findUniform1f("NearPlane")
    private val farPlane = projectorShader.findUniform1f("NearPlane")


    private val outMat = Matrix4f()
    override fun onWorldRendered(matrices: MatrixStack, camera: Camera, tickDelta: Float, nanoTime: Long) {
        val client = MinecraftClient.getInstance()
        viewPort.set(0, 0, client.window.framebufferWidth + 0, client.window.framebufferHeight + 0)
        nearPlane.set(0.1f)
        farPlane.set(1000.0f)
        if (FabricVisionClient.isRenderingProjector) {
            projectorPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
            projectorInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat))
            projectorTransformMatrix.set(outMat.invert())
        } else {
            cameraPosition.set(camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
            mainInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(outMat))
            mainTransformMatrix.set(outMat.invert())
        }
    }

    override fun renderShaderEffects(tickDelta: Float) {
        projectorShader.render(tickDelta)
    }

    private val Camera.viewMatrix: Matrix4f
        get() {
            val viewMatrix = Matrix4f()

            val direction = Vector3f(
                -cos(Math.toRadians(yaw.toDouble())).toFloat() * cos(Math.toRadians(pitch.toDouble())).toFloat(),
                -sin(Math.toRadians(pitch.toDouble())).toFloat(),
                sin(Math.toRadians(yaw.toDouble())).toFloat() * cos(Math.toRadians(pitch.toDouble())).toFloat()
            ).normalize()

            // Calculate camera right and up vectors
            val right = Vector3f(1.0f, 0.0f, 0.0f).cross(direction).normalize()
            val up = direction.cross(right)

            // Set the view matrix using JOML
            viewMatrix.set(
                right.x, up.x, -direction.x, 0.0f,
                right.y, up.y, -direction.y, 0.0f,
                right.z, up.z, -direction.z, 0.0f,
                -right.dot(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat()),
                -up.dot(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat()),
                direction.dot(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat()),
                1.0f
            )

            return viewMatrix
        }


}