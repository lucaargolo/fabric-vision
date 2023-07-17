package io.github.lucaargolo.fabricvision.common.screenhandler

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandlerType

object ScreenHandlerCompendium: RegistryCompendium<ScreenHandlerType<*>>(Registries.SCREEN_HANDLER) {

    override fun initializeClient() {
    }

}