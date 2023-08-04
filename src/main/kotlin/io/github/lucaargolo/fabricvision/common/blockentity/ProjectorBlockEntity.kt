package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.client.ProjectorProgram
import io.github.lucaargolo.fabricvision.common.block.ProjectorBlock
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationPropertyHelper
import net.minecraft.world.World

open class ProjectorBlockEntity(pos: BlockPos, state: BlockState): MediaPlayerBlockEntity(BlockEntityCompendium.PROJECTOR, pos, state) {

    var projectorProgram: ProjectorProgram? = null
    var cameraEntity: Entity? = null

    override fun setWorld(world: World?) {
        super.setWorld(world)
        if(world?.isClient == true) {
            projectorProgram = ProjectorProgram()
            val rotation = cachedState[ProjectorBlock.ROTATION]
            cameraEntity = EntityType.ARROW.create(world)
            cameraEntity?.updatePositionAndAngles(pos.x + 0.5, pos.y + 0.16 , pos.z + 0.5, RotationPropertyHelper.toDegrees(rotation), 0.0f)
        }
    }

    override fun markRemoved() {
        super.markRemoved()
        projectorProgram?.effect?.release()
    }

}