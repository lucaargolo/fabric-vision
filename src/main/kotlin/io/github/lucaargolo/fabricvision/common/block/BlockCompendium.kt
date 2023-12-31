package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object BlockCompendium: RegistryCompendium<Block>(Registries.BLOCK) {

    val HOLOGRAM = register("hologram", HologramBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val FLAT_SCREEN = register("flat_screen", FlatScreenBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val MONITOR = register("monitor", MonitorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val PANEL = register("panel", PanelBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val PROJECTOR = register("projector", ProjectorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val BOOKSHELF_SPEAKER = register("bookshelf_speaker", BookshelfSpeakerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val SPEAKER = register("speaker", SpeakerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val LARGE_SPEAKER = register("large_speaker", LargeSpeakerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))
    val DISK_RACK = register("disk_rack", DiskRackBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)))

    fun registerBlockItems(itemMap: MutableMap<Identifier, Item>) {
        map.mapValues { BlockItem(it.value, FabricItemSettings()) }.forEach(itemMap::put)
    }

    override fun initializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(HOLOGRAM, RenderLayer.getTranslucent())
    }

}