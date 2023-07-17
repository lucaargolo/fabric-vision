package io.github.lucaargolo.fabricvision.common.command.argumenttypes

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.registry.Registries

object ArgumentTypesCompendium: RegistryCompendium<ArgumentSerializer<*, *>>(Registries.COMMAND_ARGUMENT_TYPE) {


    override fun initialize() {
        super.initialize()
    }

}