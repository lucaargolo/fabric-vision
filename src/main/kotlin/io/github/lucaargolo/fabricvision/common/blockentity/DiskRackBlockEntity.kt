package io.github.lucaargolo.fabricvision.common.blockentity

import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos

class DiskRackBlockEntity(pos: BlockPos, state: BlockState) : SyncableBlockEntity(BlockEntityCompendium.DISK_RACK, pos, state) {

    val stacks = DefaultedList.ofSize(7, ItemStack.EMPTY)

    override fun writeNbt(nbt: NbtCompound) {
        repeat(stacks.size) {
            nbt.put("disk_$it", stacks[it].writeNbt(NbtCompound()))
        }
    }

    override fun readNbt(nbt: NbtCompound) {
        repeat(stacks.size) {
            stacks[it] = ItemStack.fromNbt(nbt.getCompound("disk_$it"))
        }
    }

}