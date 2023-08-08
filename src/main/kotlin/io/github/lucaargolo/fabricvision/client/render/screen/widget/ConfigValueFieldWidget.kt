package io.github.lucaargolo.fabricvision.client.render.screen.widget

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen
import io.github.lucaargolo.fabricvision.network.PacketCompendium
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text

class ConfigValueFieldWidget(private val parent: MediaPlayerScreen<*>, textRenderer: TextRenderer, x: Int, y: Int, width: Int, height: Int, private val index: Int, private val valueSupplier: () -> Float): TextFieldWidget(textRenderer, x, y, width, height, Text.empty()) {

    private var internalUpdate = false

    init {
        setDrawsBackground(false)
        setTextPredicate {
            it.count { c -> c.isDigit() } < 5 && it.count { c -> !c.isDigit() && c != '-' && c != '.' } == 0 && it.count { c -> c == '.' } in (0..1) && if(it.startsWith("-")) {
                it.count { c -> c == '-' } == 1
            }else{
                it.count { c -> c == '-' } == 0
            }
        }
        setChangedListener {
            if(!internalUpdate) {
                val buf = PacketByteBufs.create()
                buf.writeBlockPos(parent.blockEntity.pos)
                buf.writeInt(index)
                buf.writeFloat(text.toFloatOrNull() ?: 0f)
                ClientPlayNetworking.send(PacketCompendium.SET_VALUE_BUTTON_C2S, buf)
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.matrices.push()
        context.matrices.scale(0.5f, 0.5f, 0.5f)
        x *= 2
        y *= 2
        width *= 2
        height *= 2
        super.render(context, mouseX, mouseY, delta)
        height /= 2
        width /= 2
        y /= 2
        x /= 2
        context.matrices.pop()
    }

    override fun tick() {
        super.tick()
        if(!isFocused) {
            internalUpdate = true
            this.text = valueSupplier.invoke().toString()
            this.setCursorToStart()
            internalUpdate = false
        }
    }



}