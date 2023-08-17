package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.client.render.screen.widget.ColoredButtonWidget
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import java.util.*

class ImageDiskScreen(stackUUID: UUID, stack: ItemStack) : DiskScreen(stackUUID, stack, Type.IMAGE, Text.translatable("screen.fabricvision.title.image_disk")) {

    private var deleteButton: ButtonWidget? = null
    private var clearButton: ButtonWidget? = null

    override fun init() {
        super.init()
        if(stack.nbt?.contains("delete") == true) {
            mrlField?.active = false
            mrlField?.tooltip = Tooltip.of(Text.translatable("tooltip.fabricvision.disk_full"))
            val clearButtonBuilder = ButtonWidget.builder(Text.translatable("screen.fabricvision.clear_disk")) {
                clear(false)
                client?.setScreen(null)
            }
            clearButtonBuilder.tooltip(Tooltip.of(Text.translatable("tooltip.fabricvision.clear_disk")))
            clearButtonBuilder.dimensions((width/2)-151, (height/2)+20, 150, 20)
            clearButton = clearButtonBuilder.build()
            val deleteButtonBuilder = ColoredButtonWidget.builder(Text.translatable("screen.fabricvision.delete_image")) {
                client?.setScreen(ConfirmDeleteScreen(this))
            }
            deleteButtonBuilder.color(0xFF0000)
            deleteButtonBuilder.tooltip(Tooltip.of(Text.translatable("tooltip.fabricvision.delete_image")))
            deleteButtonBuilder.dimensions((width/2)+1, (height/2)+20, 150, 20)
            deleteButton = deleteButtonBuilder.build()

            addDrawableChild(clearButton)
            addDrawableChild(deleteButton)
        }
    }

    fun clear(delete: Boolean) {
        val validStack = getValidStack(client?.player) ?: return
        val hand = if(validStack == client?.player?.mainHandStack) Hand.MAIN_HAND else Hand.OFF_HAND
        val buf = PacketByteBufs.create()
        buf.writeUuid(stackUUID)
        buf.writeEnumConstant(hand)
        buf.writeBoolean(delete)
        ClientPlayNetworking.send(PacketCompendium.CLEAR_IMAGE_DISK_C2S, buf)
    }




}