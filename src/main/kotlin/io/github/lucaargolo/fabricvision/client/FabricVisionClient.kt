package io.github.lucaargolo.fabricvision.client

import io.github.lucaargolo.fabricvision.client.render.blockentity.BlockEntityRendererCompendium
import io.github.lucaargolo.fabricvision.client.render.entity.EntityRendererCompendium
import io.github.lucaargolo.fabricvision.client.render.item.DynamicItemRendererCompendium
import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import io.github.lucaargolo.fabricvision.common.command.CommandCompendium
import io.github.lucaargolo.fabricvision.common.entity.EntityCompendium
import io.github.lucaargolo.fabricvision.common.fluid.FluidCompendium
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.common.recipe.RecipeSerializerCompendium
import io.github.lucaargolo.fabricvision.common.recipe.RecipeTypeCompendium
import io.github.lucaargolo.fabricvision.common.resource.ResourceCompendium
import io.github.lucaargolo.fabricvision.common.screenhandler.ScreenHandlerCompendium
import io.github.lucaargolo.fabricvision.common.sound.SoundCompendium
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayerHolder
import io.github.lucaargolo.fabricvision.utils.FramebufferTexture
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import ladysnake.satin.api.event.PostWorldRenderCallbackV2
import ladysnake.satin.api.event.ShaderEffectRenderCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.BufferBuilderStorage
import org.joml.Matrix4f


object FabricVisionClient: ClientModInitializer {

    var isRenderingProjector = false

    val colorTexture = ModIdentifier("projector_color")
    val depthTexture = ModIdentifier("projector_depth")

    val projectorFramebuffer: SimpleFramebuffer by lazy {
        val client = MinecraftClient.getInstance()
        SimpleFramebuffer(client.window.framebufferWidth, client.window.framebufferHeight, true, MinecraftClient.IS_SYSTEM_MAC).also { framebuffer ->
            client.textureManager.registerTexture(colorTexture, FramebufferTexture(framebuffer, false))
            client.textureManager.registerTexture(depthTexture, FramebufferTexture(framebuffer, true))
        }
    }

    val inverseProjectionMatrix = Matrix4f()

    var projectorBufferBuilders = BufferBuilderStorage()


    private fun initializeRegistries() {
        BlockCompendium.initializeClient()
        ItemCompendium.initializeClient()
        FluidCompendium.initializeClient()
        BlockEntityCompendium.initializeClient()
        EntityCompendium.initializeClient()
        ResourceCompendium.initializeClient()
        PacketCompendium.initializeClient()
        RecipeSerializerCompendium.initializeClient()
        RecipeTypeCompendium.initializeClient()
        ScreenHandlerCompendium.initializeClient()
        CommandCompendium.initializeClient()
        SoundCompendium.initializeClient()
        BlockEntityRendererCompendium.initializeClient()
        EntityRendererCompendium.initializeClient()
        DynamicItemRendererCompendium.initializeClient()
    }
    override fun onInitializeClient() {
        initializeRegistries()
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            MinecraftMediaPlayerHolder.clientTick(client)
        }
        ClientTickEvents.END_WORLD_TICK.register {
            MinecraftMediaPlayerHolder.worldTick()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            MinecraftMediaPlayerHolder.close(false)
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            MinecraftMediaPlayerHolder.close(true)
        }
        ShaderEffectRenderCallback.EVENT.register(ProjectorShader)
        PostWorldRenderCallbackV2.EVENT.register(ProjectorShader)
    }

}