package io.github.lucaargolo.fabricvision.client

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import ladysnake.satin.api.managed.ManagedShaderEffect
import ladysnake.satin.api.managed.ShaderEffectManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30

object HandHelper {

    private val HAND_SOLID_SHADER = ModIdentifier("shaders/post/hand_solid.json")
    private val HAND_TRANSLUCENT_SHADER = ModIdentifier("shaders/post/hand_translucent.json")

    var renderingHand = false

    val handSolidFramebuffer: SimpleFramebuffer by lazy {
        SimpleFramebuffer(854, 480, true, MinecraftClient.IS_SYSTEM_MAC).also {
            it.setClearColor(0f, 0f, 0f, 0f)
        }
    }

    val handTranslucentFramebuffer: SimpleFramebuffer by lazy {
        SimpleFramebuffer(854, 480, true, MinecraftClient.IS_SYSTEM_MAC).also {
            it.setClearColor(0f, 0f, 0f, 0f)
        }
    }

    private val handSolidEffect: ManagedShaderEffect by lazy {
        val client = MinecraftClient.getInstance()
        ShaderEffectManager.getInstance().manage(HAND_SOLID_SHADER) { shader: ManagedShaderEffect ->
            shader.setSamplerUniform("MainSampler", client.framebuffer.colorAttachment)
            shader.setSamplerUniform("HandSampler", handSolidFramebuffer.colorAttachment)
        }
    }

    private val handTranslucentEffect: ManagedShaderEffect by lazy {
        val client = MinecraftClient.getInstance()
        ShaderEffectManager.getInstance().manage(HAND_TRANSLUCENT_SHADER) { shader: ManagedShaderEffect ->
            shader.setSamplerUniform("MainSampler", client.framebuffer.colorAttachment)
            shader.setSamplerUniform("HandSampler", handTranslucentFramebuffer.colorAttachment)
        }
    }

    fun startRender() {
        val prevBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        handSolidFramebuffer.beginWrite(true)
        handSolidFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        handTranslucentFramebuffer.beginWrite(true)
        handTranslucentFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, prevBuffer)
    }

    fun finishRender(tickDelta: Float) {
        handSolidEffect.render(tickDelta)
        handTranslucentEffect.render(tickDelta)
    }



}