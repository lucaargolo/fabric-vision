package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.common.block.HorizontalFacingMediaPlayerBlock
import io.github.lucaargolo.fabricvision.utils.ModConfig
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.jvm.optionals.getOrNull

class PanelBlockEntity(pos: BlockPos, state: BlockState) : MediaPlayerBlockEntity(BlockEntityCompendium.PANEL, pos, state) {

    var activePanelPos: BlockPos? = null
        set(value) {
            field = value
            markDirtyAndSync()
        }
    var activePosSet: MutableSet<BlockPos> = mutableSetOf()
        set(value) {
            field = value
            markDirtyAndSync()
        }

    val activePanel: PanelBlockEntity?
        get() {
            return if(activePanelPos == null) {
                null
            } else {
                if (activePanelPos == pos) {
                    this
                } else {
                    world?.getBlockEntity(activePanelPos, BlockEntityCompendium.PANEL)?.getOrNull()
                }
            }
        }

    var currentXSize = 0
    var currentYSize = 0
    var currentMinPos = BlockPos.ORIGIN
    var currentMaxPos = BlockPos.ORIGIN

    init {
        changeEnable(false)
    }

    override fun changeEnable(enabled: Boolean) {
        if(!enabled) {
            super.changeEnable(false)
        }else if(activePanelPos == pos && activePosSet.contains(pos)) {
            super.changeEnable(true)
        }
    }

    override fun getCenterPos(): Vec3d {
        val b = BlockBox.create(currentMinPos, currentMaxPos)
        return Vec3d(b.minX + (b.maxX - b.minX )/2.0, b.minY + (b.maxY - b.minY)/2.0, b.minZ + (b.maxZ - b.minZ)/2.0)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        activePanelPos?.let {
            nbt.putLong("activePanelPos", it.asLong())
            nbt.putLongArray("activePosSet", activePosSet.map(BlockPos::asLong).toLongArray())
            nbt.putInt("currentXSize", currentXSize)
            nbt.putInt("currentYSize", currentYSize)
            nbt.putLong("currentMinPos", currentMinPos.asLong())
            nbt.putLong("currentMaxPos", currentMaxPos.asLong())
        }
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        activePanelPos = if(nbt.contains("activePanelPos")) BlockPos.fromLong(nbt.getLong("activePanelPos")) else null
        activePosSet = nbt.getLongArray("activePosSet").map(BlockPos::fromLong).toMutableSet()
        currentXSize = nbt.getInt("currentXSize")
        currentYSize = nbt.getInt("currentYSize")
        currentMinPos = BlockPos.fromLong(nbt.getLong("currentMinPos"))
        currentMaxPos = BlockPos.fromLong(nbt.getLong("currentMaxPos"))

    }

    override fun play() {
        if(activePanel == this) {
            super.play()
        }else{
            activePanel?.play()
        }
    }

    override fun pause() {
        if(activePanel == this) {
            super.pause()
        }else{
            activePanel?.pause()
        }
    }

    fun setup(world: ServerWorld, facing: Direction, pos: BlockPos) {
        val nearbyPanels = mutableSetOf<PanelBlockEntity>()
        Direction.values().forEach { direction ->
            if(direction.axis != facing.axis) {
                val state = world.getBlockState(pos.offset(direction))
                if(state.isOf(BlockCompendium.PANEL) && state[HorizontalFacingMediaPlayerBlock.FACING] == facing) {
                    world.getBlockEntity(pos.offset(direction), BlockEntityCompendium.PANEL).ifPresent { nearbyPanel ->
                        nearbyPanel.activePanel?.let(nearbyPanels::add)
                    }
                }
            }
        }
        if(nearbyPanels.size <= 1) {
            val nearbyPanel = nearbyPanels.firstOrNull() ?: this
            val (minPos, maxPos, found) = nearbyPanel.searchPanels(world, facing, pos)

            val blockBox = BlockBox.create(minPos, maxPos)

            val xSize = if(facing.axis == Direction.Axis.X) blockBox.blockCountZ else blockBox.blockCountX
            val ySize = blockBox.blockCountY

            val isValid = (xSize * ySize) == found.size

            if(isValid) {
                world.getBlockEntity(minPos, BlockEntityCompendium.PANEL).ifPresent { newActivePanel ->
                    newActivePanel.activePanelPos = newActivePanel.pos
                    newActivePanel.activePosSet = found
                    newActivePanel.currentXSize = xSize
                    newActivePanel.currentYSize = ySize
                    newActivePanel.currentMinPos = minPos
                    newActivePanel.currentMaxPos = maxPos
                    if(newActivePanel != nearbyPanel && nearbyPanel.activePanelPos != null) {
                        nearbyPanel.changeEnable(false)
                        nearbyPanel.activePanelPos = newActivePanel.pos
                        nearbyPanel.activePosSet = linkedSetOf()
                        nearbyPanel.currentXSize = 0
                        nearbyPanel.currentYSize = 0
                        nearbyPanel.currentMinPos = BlockPos.ORIGIN
                        nearbyPanel.currentMaxPos = BlockPos.ORIGIN
                    }
                    found.forEach { foundPos ->
                        world.getBlockEntity(foundPos, BlockEntityCompendium.PANEL).ifPresent {
                            it.activePanelPos = newActivePanel.pos
                            if(it.activePanelPos != foundPos) {
                                it.diskStack?.let { diskStack ->
                                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, diskStack)
                                }
                                it.diskStack = null
                            }
                        }
                    }
                }
            }
        }else{
            nearbyPanels.forEach { nearbyPanel ->
                nearbyPanel.disable(world)
            }
            setup(world, facing, pos)
        }
    }

    fun disable(world: ServerWorld) {
        activePanelPos = null
        activePosSet.forEach { foundPos ->
            world.getBlockEntity(foundPos, BlockEntityCompendium.PANEL).ifPresent {
                if(it.activePanelPos != null) it.disable(world)
            }
        }
        activePosSet.clear()
        currentXSize = 0
        currentYSize = 0
        currentMinPos = BlockPos.ORIGIN
        currentMaxPos = BlockPos.ORIGIN

        changeEnable(false)
        markDirtyAndSync()
    }

    private fun searchPanels(world: ServerWorld, facing: Direction, pos: BlockPos, search: MutableSet<BlockPos> = linkedSetOf(), found: MutableSet<BlockPos> = linkedSetOf(), minPos: BlockPos = pos, maxPos: BlockPos = pos, depth: Int = 0): Triple<BlockPos, BlockPos, MutableSet<BlockPos>> {
        var newMinPos = minPos
        var newMaxPos = maxPos
        if(depth < ModConfig.instance.maxPanelDepth && search.add(pos)) {
            val state = world.getBlockState(pos)
            if(state.isOf(BlockCompendium.PANEL) && found.add(pos)) {
                checkMinMaxPos(facing, newMinPos, newMaxPos, pos, pos).let {
                    newMinPos = it.first
                    newMaxPos = it.second
                }
                Direction.values().forEach { direction ->
                    if (direction.axis != facing.axis) {
                        val (foundMinPos, foundMaxPos) = searchPanels(world, facing, pos.offset(direction), search, found, newMinPos, newMaxPos, depth + 1)
                        checkMinMaxPos(facing, newMinPos, newMaxPos, foundMinPos, foundMaxPos).let {
                            newMinPos = it.first
                            newMaxPos = it.second
                        }
                    }
                }
            }
        }
        return Triple(newMinPos, newMaxPos, found)
    }

    private fun checkMinMaxPos(facing: Direction, oldMinPos: BlockPos, oldMaxPos: BlockPos, minPos: BlockPos, maxPos: BlockPos): Pair<BlockPos, BlockPos> {
        var newMinPos = minPos
        var newMaxPos = maxPos
        if (oldMinPos.y < newMinPos.y) {
            newMinPos = oldMinPos
        }
        if (oldMaxPos.y > newMaxPos.y) {
            newMaxPos = oldMaxPos
        }
        if (facing.axis == Direction.Axis.X) {
            if (facing.direction == Direction.AxisDirection.POSITIVE) {
                //negative z > positive z
                if (oldMinPos.z > newMinPos.z) {
                    newMinPos = oldMinPos
                }
                if (oldMaxPos.z < newMaxPos.z) {
                    newMaxPos = oldMaxPos
                }
            } else {
                //positive z > negative z
                if (oldMinPos.z < newMinPos.z) {
                    newMinPos = oldMinPos
                }
                if (oldMaxPos.z > newMaxPos.z) {
                    newMaxPos = oldMaxPos
                }
            }
        } else {
            if (facing.direction == Direction.AxisDirection.POSITIVE) {
                //positive x > negative x
                if (oldMinPos.x < newMinPos.x) {
                    newMinPos = oldMinPos
                }
                if (oldMaxPos.x > newMaxPos.x) {
                    newMaxPos = oldMaxPos
                }
            } else {
                //negative x > positive x
                if (oldMinPos.x > newMinPos.x) {
                    newMinPos = oldMinPos
                }
                if (oldMaxPos.x < newMaxPos.x) {
                    newMaxPos = oldMaxPos
                }
            }
        }
        return newMinPos to newMaxPos
    }


}