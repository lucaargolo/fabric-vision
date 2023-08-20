package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.common.item.DiskItem
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import java.util.*

open class DiskScreen(val stackUUID: UUID, val stack: ItemStack, val type: Type, title: Text): Screen(title) {

    protected var nameField: TextFieldWidget? = null
    protected var mrlField: TextFieldWidget? = null

    override fun init() {
        super.init()
        nameField = TextFieldWidget(textRenderer, (width/2)-150, (height/2)-45, 300, 16, Text.empty())
        nameField?.setMaxLength(55)
        nameField?.text = stack.nbt?.getString("name") ?: ""
        nameField?.setEditableColor(16777215)
        nameField?.setChangedListener {
            update()
        }
        mrlField = TextFieldWidget(textRenderer, (width/2)-150, (height/2)-5, 300, 16, Text.empty())
        mrlField?.setMaxLength(99999)
        mrlField?.text = stack.nbt?.getString("mrl") ?: ""
        mrlField?.setEditableColor(16777215)
        mrlField?.setChangedListener {
            update()
        }
        this.addDrawableChild(nameField)
        this.addDrawableChild(mrlField)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val mrlField = mrlField ?: return super.keyPressed(keyCode, scanCode, modifiers)
        return if (mrlField.keyPressed(keyCode, scanCode, modifiers)) true
        else if (mrlField.isFocused && mrlField.isVisible && keyCode != 256) true
        else super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val setNameText = Text.translatable("tooltip.fabricvision.set_name")
        context.drawText(textRenderer, setNameText, (width/2)-(textRenderer.getWidth(setNameText)/2), (height/2)-60, 0xFFFFFF, false)
        val setMrlText = Text.translatable("tooltip.fabricvision.set_mrl")
        context.drawText(textRenderer, setMrlText, (width/2)-(textRenderer.getWidth(setMrlText)/2), (height/2)-20, 0xFFFFFF, false)
    }

    override fun tick() {
        super.tick()
        if(getValidStack(client?.player) == null) {
            client?.setScreen(null)
        }
    }

    protected fun getValidStack(player: PlayerEntity?): ItemStack? {
        player ?: return null
        return if(player.mainHandStack.item is DiskItem && player.mainHandStack.nbt?.contains("uuid") == true && player.mainHandStack.nbt?.getUuid("uuid") == stackUUID) {
            player.mainHandStack
        }else if(player.offHandStack.item is DiskItem && player.offHandStack.nbt?.contains("uuid") == true && player.offHandStack.nbt?.getUuid("uuid") == stackUUID){
            player.offHandStack
        }else{
            null
        }
    }

    protected open fun update() {
        val validStack = getValidStack(client?.player) ?: return
        val hand = if(validStack == client?.player?.mainHandStack) Hand.MAIN_HAND else Hand.OFF_HAND
        val name = nameField?.text ?: ""
        val mrl = mrlField?.text ?: ""
        val buf = PacketByteBufs.create()
        buf.writeUuid(stackUUID)
        buf.writeEnumConstant(hand)
        buf.writeEnumConstant(type)
        buf.writeString(name)
        buf.writeString(mrl)
        ClientPlayNetworking.send(PacketCompendium.UPDATE_DISK_C2S, buf)
    }

    override fun shouldPause() = false

}