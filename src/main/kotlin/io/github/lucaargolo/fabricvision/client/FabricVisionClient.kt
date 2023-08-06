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
import ladysnake.satin.api.event.PostWorldRenderCallbackV2
import ladysnake.satin.api.event.ShaderEffectRenderCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW


object FabricVisionClient: ClientModInitializer {

    var renderingProjector: ProjectorProgram? = null
    val isRenderingProjector
        get() = renderingProjector != null

    val isSneaking: Boolean
        get() {
            val client: MinecraftClient = MinecraftClient.getInstance()
            val handle: Long = client.window.handle
            val sneakKey: KeyBinding = client.options.sneakKey
            val boundKey: InputUtil.Key = sneakKey.boundKey
            var sneak = false
            if (boundKey.category == InputUtil.Type.MOUSE) {
                sneak = GLFW.glfwGetMouseButton(handle, boundKey.code) == 1
            } else if (boundKey.category == InputUtil.Type.KEYSYM) {
                sneak = GLFW.glfwGetKey(handle, boundKey.code) == 1
            }
            return sneak
        }

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
        ClientTickEvents.START_CLIENT_TICK.register { client ->
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
        ShaderEffectRenderCallback.EVENT.register(ProjectorProgram::renderProjectors)
        PostWorldRenderCallbackV2.EVENT.register(ProjectorProgram::captureCameras)
    }

}