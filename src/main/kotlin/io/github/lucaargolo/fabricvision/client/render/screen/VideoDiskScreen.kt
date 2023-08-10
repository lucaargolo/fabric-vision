package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.common.item.ItemCompendium
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.CheckboxWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import java.util.*

class VideoDiskScreen(val stackUUID: UUID, val stack: ItemStack): Screen(Text.translatable("screen.fabricvision.title.video_disk")) {

    private var mrlField: TextFieldWidget? = null
    private var streamCheckbox: CheckboxWidget? = null

    override fun init() {
        super.init()
        mrlField = TextFieldWidget(textRenderer, (width/2)-150, (height/2)-5, 300, 16, Text.empty())
        mrlField?.setMaxLength(99999)
        mrlField?.text = stack.nbt?.getString("mrl") ?: ""
        mrlField?.setEditableColor(16777215)
        mrlField?.setChangedListener(::update)
        streamCheckbox = object: CheckboxWidget((width/2)+150-19, (height/2)+20, 18, 20, Text.empty(), false) {
            override fun onPress() {
                super.onPress()
                update(forceCheck = true)
            }
        }
        this.addDrawableChild(mrlField)
        this.addDrawableChild(streamCheckbox)
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
        val setMrlText = Text.translatable("screen.fabricvision.message.set_mrl").append(": ")
        context.drawText(textRenderer, setMrlText, (width/2)-(textRenderer.getWidth(setMrlText)/2), (height/2)-20, 0xFFFFFF, false)
        val streamText = Text.translatable("screen.fabricvision.message.stream")
        context.drawText(textRenderer, streamText, (width/2)+150-22-textRenderer.getWidth(streamText), (height/2)+26, 0xFFFFFF, false)
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

    private fun update(mrl: String = mrlField?.text ?: "", forceCheck: Boolean = false) {
        val validStack = getValidStack(client?.player) ?: return
        val hand = if(validStack == client?.player?.mainHandStack) Hand.MAIN_HAND else Hand.OFF_HAND
        val stream = mrl.endsWith(".m3u8") || mrl.startsWith("rt") || mrl.startsWith("mms")
        if(stream != streamCheckbox?.isChecked && !forceCheck) {
            streamCheckbox?.onPress()
        }
        val buf = PacketByteBufs.create()
        buf.writeUuid(stackUUID)
        buf.writeEnumConstant(hand)
        buf.writeString(mrl)
        buf.writeBoolean(streamCheckbox?.isChecked ?: stream)
        ClientPlayNetworking.send(PacketCompendium.SET_VIDEO_DISK_MRL_C2S, buf)
    }

    override fun shouldPause() = false

}