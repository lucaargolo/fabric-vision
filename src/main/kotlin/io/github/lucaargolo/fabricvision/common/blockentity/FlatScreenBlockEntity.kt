package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.utils.MinecraftMediaPlayer
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import java.util.*

class FlatScreenBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntityCompendium.FLAT_SCREEN, pos, state) {

    var uuid = UUID.randomUUID()
    var player = MinecraftMediaPlayer.create(uuid)

    override fun markRemoved() {
        super.markRemoved()
        if(world?.isClient == true) {
            player.close()
        }
    }

}