package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries

object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {

    val FLAT_SCREEN = register("flat_screen", FabricBlockEntityTypeBuilder.create(::FlatScreenBlockEntity, BlockCompendium.FLAT_SCREEN).build())
    val MONITOR = register("monitor", FabricBlockEntityTypeBuilder.create(::MonitorBlockEntity, BlockCompendium.MONITOR).build())
    val PANEL = register("panel", FabricBlockEntityTypeBuilder.create(::PanelBlockEntity, BlockCompendium.PANEL).build())
    val PROJECTOR = register("projector", FabricBlockEntityTypeBuilder.create(::ProjectorBlockEntity, BlockCompendium.PROJECTOR).build())
    val HOLOGRAM = register("hologram", FabricBlockEntityTypeBuilder.create(::HologramBlockEntity, BlockCompendium.HOLOGRAM).build())
    val BOOKSHELF_SPEAKER = register("bookshelf_speaker", FabricBlockEntityTypeBuilder.create(::BookshelfSpeakerBlockEntity, BlockCompendium.BOOKSHELF_SPEAKER).build())

}