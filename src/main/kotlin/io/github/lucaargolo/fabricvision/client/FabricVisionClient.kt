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
import io.github.lucaargolo.fabricvision.utils.MediaPlayerHolder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object FabricVisionClient: ClientModInitializer {

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
        ClientLifecycleEvents.CLIENT_STARTED.register {
            MediaPlayerHolder.start()
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            MediaPlayerHolder.tick()
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            MediaPlayerHolder.stop()
        }
    }

}