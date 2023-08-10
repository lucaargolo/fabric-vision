package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.Direction

abstract class HorizontalFacingMediaPlayerBlock<T: MediaPlayerBlockEntity>(typeProvider: () -> BlockEntityType<T>, settings: Settings) : MediaPlayerBlock<T>(typeProvider, settings) {

    init {
        defaultState = defaultState.with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val direction = ctx.horizontalPlayerFacing.opposite
        return defaultState.with(FACING, direction)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("state.with(FACING, rotation.rotate(state.get(FACING) as Direction)) as BlockState", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "net.minecraft.util.math.Direction", "net.minecraft.block.BlockState"))
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
        return state.with(FACING, rotation.rotate(state.get(FACING) as Direction)) as BlockState
    }

    @Deprecated("Deprecated in Java", ReplaceWith("state.rotate(mirror.getRotation(state.get(FACING) as Direction))", "io.github.lucaargolo.fabricvision.common.block.PanelBlock.Companion.FACING", "net.minecraft.util.math.Direction"))
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
        return state.rotate(mirror.getRotation(state.get(FACING) as Direction))
    }
    companion object {

        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING


    }

}