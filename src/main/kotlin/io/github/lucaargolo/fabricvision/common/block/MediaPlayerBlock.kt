package io.github.lucaargolo.fabricvision.common.block

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.common.blockentity.MediaPlayerBlockEntity
import io.github.lucaargolo.fabricvision.common.item.DiskItem
import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.common.sound.SoundCompendium
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import kotlin.jvm.optionals.getOrNull

abstract class MediaPlayerBlock<T: MediaPlayerBlockEntity>(private val typeProvider: () -> BlockEntityType<T>, settings: Settings) : BlockWithEntity(settings), InventoryProvider {

    open fun getOriginalPos(world: WorldAccess, state: BlockState, pos: BlockPos): BlockPos? = pos

    open fun getScreen(entity: T): MediaPlayerScreen<T> = MediaPlayerScreen(entity)

    protected fun getBlockEntity(world: WorldAccess, state: BlockState, pos: BlockPos): T? {
        val originalPos = getOriginalPos(world, state, pos) ?: return null
        return world.getBlockEntity(originalPos, typeProvider.invoke()).getOrNull()
    }

    override fun getInventory(state: BlockState, world: WorldAccess, pos: BlockPos): SidedInventory? {
        return getBlockEntity(world, state, pos)?.let {
            return MediaPlayerInventory(it)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            getBlockEntity(world, state, pos)?.let {
                it.diskStack?.let { diskStack ->
                    ItemScatterer.spawn(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, diskStack)
                }
                it.diskStack = null
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        getBlockEntity(world, state, pos)?.let {
            val stack = player.getStackInHand(hand)
            if(stack.item is DiskItem) {
                if(!world.isClient) {
                    it.diskStack?.let { diskStack ->
                        val vec = player.horizontalFacing.opposite.unitVector
                        ItemScatterer.spawn(world, pos.x + 0.5 + vec.x, pos.y + 0.5 + vec.y, pos.z + 0.5 + vec.z, diskStack)
                    }
                    it.diskStack = stack.copy()
                    stack.decrement(1)
                    it.markDirtyAndSync()
                    it.play()
                    world.playSound(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, SoundCompendium.DISK_INSERT, SoundCategory.BLOCKS, 1f, 1f)
                }
            }else if(player.isSneaking) {
                if(!world.isClient && it.diskStack != null) {
                    it.diskStack?.let { diskStack ->
                        val vec = player.horizontalFacing.opposite.unitVector
                        ItemScatterer.spawn(world, pos.x + 0.5 + vec.x, pos.y + 0.5 + vec.y, pos.z + 0.5 + vec.z, diskStack)
                    }
                    it.diskStack = null
                    it.markDirtyAndSync()
                    it.play()
                    world.playSound(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, SoundCompendium.DISK_EXTRACT, SoundCategory.BLOCKS, 1f, 1f)
                }
            }else if(world.isClient) {
                MinecraftClient.getInstance().setScreen(getScreen(it))
            }
        }
        return ActionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if(world.isClient)
            checkType(type, typeProvider.invoke(), MediaPlayerBlockEntity::clientTick)
        else
            checkType(type, typeProvider.invoke(), MediaPlayerBlockEntity::serverTick)
    }

    class MediaPlayerInventory(private val blockEntity: MediaPlayerBlockEntity): SidedInventory {
        override fun clear() {
            blockEntity.diskStack = null
        }

        override fun size() = 1

        override fun isEmpty(): Boolean {
            return blockEntity.diskStack == null || blockEntity.diskStack?.isEmpty != false
        }

        override fun getStack(slot: Int): ItemStack {
            return blockEntity.diskStack ?: ItemStack.EMPTY
        }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            return removeStack(slot)
        }

        override fun removeStack(slot: Int): ItemStack {
            return blockEntity.diskStack?.also { blockEntity.diskStack = null } ?: ItemStack.EMPTY
        }

        override fun setStack(slot: Int, stack: ItemStack) {
            blockEntity.diskStack = stack
        }

        override fun markDirty() {
            if(blockEntity.diskStack?.isEmpty != false) {
                blockEntity.world?.playSound(null, blockEntity.pos.x + 0.5, blockEntity.pos.y + 0.5, blockEntity.pos.z + 0.5, SoundCompendium.DISK_INSERT, SoundCategory.BLOCKS, 1f, 1f)
            }else{
                blockEntity.world?.playSound(null, blockEntity.pos.x + 0.5, blockEntity.pos.y + 0.5, blockEntity.pos.z + 0.5, SoundCompendium.DISK_EXTRACT, SoundCategory.BLOCKS, 1f, 1f)
            }
            blockEntity.markDirtyAndSync()
            blockEntity.play()
        }

        override fun canPlayerUse(player: PlayerEntity) = true

        override fun getAvailableSlots(side: Direction) = intArrayOf(0)

        override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean {
            return stack.isOf(ItemCompendium.VIDEO_DISK) && (blockEntity.diskStack == null || blockEntity.diskStack?.isEmpty != false)
        }

        override fun canExtract(slot: Int, stack: ItemStack?, dir: Direction?): Boolean {
            return true
        }

    }

}