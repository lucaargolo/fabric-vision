package io.github.lucaargolo.fabricvision.common.blockentity

import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

open class ProjectorBlockEntity(pos: BlockPos, state: BlockState): MediaPlayerBlockEntity(BlockEntityCompendium.PROJECTOR, pos, state) {

    var cameraEntity: Entity? = null

    override fun setWorld(world: World?) {
        super.setWorld(world)
        cameraEntity = EntityType.ARROW.create(world)
        cameraEntity?.updatePositionAndAngles(pos.x + 0.5, pos.y + 0.5 - 1.625, pos.z + 0.5, 90.0f, 0.0f)
    }

}