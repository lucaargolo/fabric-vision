@file:Suppress("UNUSED_PARAMETER", "UNUSED_ANONYMOUS_PARAMETER", "UNUSED_VARIABLE", "UNUSED")

package io.github.lucaargolo.fabricvision

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
import io.github.lucaargolo.fabricvision.player.SetupLibVLC
import io.github.lucaargolo.fabricvision.utils.ModConfig
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object FabricVision: ModInitializer {

    const val MOD_ID = "fabricvision"
    const val MOD_NAME = "Fabric Vision"

    private fun initializeRegistries() {
        BlockCompendium.initialize()
        ItemCompendium.initialize()
        FluidCompendium.initialize()
        BlockEntityCompendium.initialize()
        EntityCompendium.initialize()
        ResourceCompendium.initialize()
        PacketCompendium.initialize()
        RecipeSerializerCompendium.initialize()
        RecipeTypeCompendium.initialize()
        ScreenHandlerCompendium.initialize()
        CommandCompendium.initialize()
        SoundCompendium.initialize()
    }

    override fun onInitialize() {
        initializeRegistries()
        ModConfig.getInstance()
        SetupLibVLC.initialize()
    }

}


