package io.github.lucaargolo.fabricvision.client.render.item

import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry

object DynamicItemRendererCompendium {

    fun initializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(ItemCompendium.DIGITAL_CAMERA, DigitalCameraDynamicItemRenderer())
    }

}