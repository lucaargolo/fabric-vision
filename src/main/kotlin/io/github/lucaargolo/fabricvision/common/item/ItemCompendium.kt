package io.github.lucaargolo.fabricvision.common.item

import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text

object ItemCompendium: RegistryCompendium<Item>(Registries.ITEM) {

    val defaultStack: ItemStack
        get() = BlockCompendium.FLAT_SCREEN.asItem().defaultStack

    val items: Collection<Item>
        get() = map.values

    init {
        BlockCompendium.registerBlockItems(map)
    }

    val VIDEO_DISK = register("video_disk", VideoDiskItem(Item.Settings().maxCount(1)))
    val AUDIO_DISK = register("audio_disk", AudioDiskItem(Item.Settings().maxCount(1)))

    private fun registerCreativeTab() {
        Registry.register(Registries.ITEM_GROUP, ModIdentifier("creative_tab"), FabricItemGroup
            .builder()
            .displayName(Text.translatable("itemGroup.fabricvision"))
            .icon { defaultStack }
            .entries { _, entries ->
                items.forEach(entries::add)
            }
            .build()
        )
    }

    override fun initialize() {
        super.initialize()
        registerCreativeTab()
    }

    override fun initializeClient() {
        ModelPredicateProviderRegistry.register(VIDEO_DISK, ModIdentifier("stream")) { stack, _, _, _ ->
            if (stack.nbt?.getBoolean("stream") == true) 1.0f else 0.0f
        }
    }

}