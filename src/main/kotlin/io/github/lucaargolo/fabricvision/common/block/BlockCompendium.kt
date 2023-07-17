package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object BlockCompendium: RegistryCompendium<Block>(Registries.BLOCK) {

    val FLAT_SCREEN = register("flat_screen", FlatScreenBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))


    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.mapValues { BlockItem(it.value, FabricItemSettings()) }.forEach(itemMap::put)
    }

    override fun initializeClient() {

    }

}