package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries

object BlockEntityCompendium: RegistryCompendium<BlockEntityType<*>>(Registries.BLOCK_ENTITY_TYPE) {

    val FLAT_SCREEN = register("flat_screen", FabricBlockEntityTypeBuilder.create(::FlatScreenBlockEntity, BlockCompendium.FLAT_SCREEN).build())

}