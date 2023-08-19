package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.blockentity.BlockEntityCompendium
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

object BlockEntityRendererCompendium {

    fun initializeClient() {
        BlockEntityRendererFactories.register(BlockEntityCompendium.FLAT_SCREEN, ::FlatScreenBlockEntityRenderer)
        BlockEntityRendererFactories.register(BlockEntityCompendium.MONITOR, ::MonitorBlockEntityRenderer)
        BlockEntityRendererFactories.register(BlockEntityCompendium.PANEL, ::PanelBlockEntityRenderer)
        BlockEntityRendererFactories.register(BlockEntityCompendium.PROJECTOR, ::ProjectorBlockEntityRenderer)
        BlockEntityRendererFactories.register(BlockEntityCompendium.HOLOGRAM, ::HologramBlockEntityRenderer)
        BlockEntityRendererFactories.register(BlockEntityCompendium.BOOKSHELF_SPEAKER, ::BookshelfSpeakerBlockEntityRenderer)
    }

}