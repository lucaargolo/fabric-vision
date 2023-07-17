package io.github.lucaargolo.fabricvision.common.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.lucaargolo.fabricvision.common.command.argumenttypes.ArgumentTypesCompendium
import io.github.lucaargolo.fabricvision.utils.GenericCompendium
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.ServerCommandSource

object CommandCompendium: GenericCompendium<LiteralArgumentBuilder<ServerCommandSource>>() {


    override fun initialize() {
        ArgumentTypesCompendium.initialize()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            map.values.forEach(dispatcher::register)
        }
    }

    override fun initializeClient() {

    }

}