package io.github.lucaargolo.fabricvision.client.render.item

import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry

object DynamicItemRendererCompendium {

    fun initializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(ItemCompendium.DIGITAL_CAMERA, DigitalCameraDynamicItemRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(ItemCompendium.VIDEO_DISK, DiskDynamicItemRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(ItemCompendium.AUDIO_DISK, DiskDynamicItemRenderer())
        BuiltinItemRendererRegistry.INSTANCE.register(ItemCompendium.IMAGE_DISK, DiskDynamicItemRenderer())

    }

}