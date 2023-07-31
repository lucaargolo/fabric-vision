package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.utils.FramebufferTexture
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.command.argument.EntityAnchorArgumentType
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
        cameraEntity?.setPosition(Vec3d.ofCenter(pos.up().up()))
        cameraEntity?.updatePositionAndAngles(38.5, 64.5, 46.5, 90.0f, 0.0f)
    }

}