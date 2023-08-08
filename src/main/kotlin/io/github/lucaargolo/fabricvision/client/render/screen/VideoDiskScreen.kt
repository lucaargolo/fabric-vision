package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
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
import java.util.UUID

class VideoDiskScreen(val stackUUID: UUID, val stack: ItemStack): Screen(Text.translatable("screen.fabricvision.title.video_disk")) {

    private var mrlField: TextFieldWidget? = null

    override fun init() {
        super.init()
        mrlField = TextFieldWidget(textRenderer, (width/2)-150, (height/2)-5, 300, 16, Text.empty())
        mrlField?.setMaxLength(999)
        mrlField?.text = stack.nbt?.getString("mrl") ?: ""
        mrlField?.setEditableColor(16777215)
        mrlField?.setChangedListener {
            val validStack = getValidStack(client?.player) ?: return@setChangedListener
            val hand = if(validStack == client?.player?.mainHandStack) Hand.MAIN_HAND else Hand.OFF_HAND
            val buf = PacketByteBufs.create()
            buf.writeUuid(stackUUID)
            buf.writeEnumConstant(hand)
            buf.writeString(it)
            ClientPlayNetworking.send(PacketCompendium.SET_VIDEO_DISK_MRL_C2S, buf)
        }
        this.addDrawableChild(mrlField)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val nameField = mrlField ?: return super.keyPressed(keyCode, scanCode, modifiers)
        return if (nameField.keyPressed(keyCode, scanCode, modifiers)) true
        else if (nameField.isFocused && nameField.isVisible && keyCode != 256) true
        else super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val text = Text.translatable("screen.fabricvision.message.set_mrl").append(": ")
        context.drawText(textRenderer, text, (width/2)-(textRenderer.getWidth(text)/2), (height/2)-20, 0xFFFFFF, false)
    }

    override fun tick() {
        super.tick()
        if(getValidStack(client?.player) == null) {
            client?.setScreen(null)
        }
    }

    private fun getValidStack(player: PlayerEntity?): ItemStack? {
        player ?: return null
        return if(player.mainHandStack.isOf(ItemCompendium.VIDEO_DISK) && player.mainHandStack.nbt?.getUuid("uuid") == stackUUID) {
            player.mainHandStack
        }else if(player.offHandStack.isOf(ItemCompendium.VIDEO_DISK) && player.offHandStack.nbt?.getUuid("uuid") == stackUUID){
            player.offHandStack
        }else{
            null
        }
    }

    override fun shouldPause() = false

}