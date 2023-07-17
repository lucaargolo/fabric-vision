package io.github.lucaargolo.fabricvision.common.fluid

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.fluid.Fluid
import net.minecraft.registry.Registries

object FluidCompendium: RegistryCompendium<Fluid>(Registries.FLUID) {

    override fun initializeClient() {
    }


}