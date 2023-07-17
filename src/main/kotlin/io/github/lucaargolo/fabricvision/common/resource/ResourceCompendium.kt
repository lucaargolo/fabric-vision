package io.github.lucaargolo.fabricvision.common.resource

import io.github.lucaargolo.fabricvision.utils.GenericCompendium
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceType

object ResourceCompendium: GenericCompendium<SimpleSynchronousResourceReloadListener>() {

    override fun initialize() {
        map.forEach { (_, resource) ->
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(resource)
        }
    }

    override fun initializeClient() {

    }

}