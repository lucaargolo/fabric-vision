package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import net.minecraft.item.Item

abstract class DiskItem(val type: Type, settings: Settings) : Item(settings) {

    enum class Type {
        NONE,
        VIDEO,
        AUDIO,
        IMAGE
    }
    
}