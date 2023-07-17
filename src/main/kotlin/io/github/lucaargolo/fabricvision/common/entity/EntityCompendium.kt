package io.github.lucaargolo.fabricvision.common.entity

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries

object EntityCompendium: RegistryCompendium<EntityType<*>>(Registries.ENTITY_TYPE) {


    override fun initialize() {
        super.initialize()
    }

}
