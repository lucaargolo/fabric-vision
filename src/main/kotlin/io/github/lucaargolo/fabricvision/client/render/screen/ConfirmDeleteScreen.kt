package io.github.lucaargolo.fabricvision.client.render.screen

import io.github.lucaargolo.fabricvision.client.render.screen.widget.ColoredButtonWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class ConfirmDeleteScreen(val parent: ImageDiskScreen) : Screen(Text.translatable("screen.fabricvision.title.confirm_delete")) {

    private var deleteButton: ButtonWidget? = null
    private var clearButton: ButtonWidget? = null

    override fun init() {
        super.init()
        val returnButtonBuilder = ButtonWidget.builder(Text.translatable("screen.fabricvision.cancel")) {
            client?.setScreen(parent)
        }
        returnButtonBuilder.dimensions((width/2)-151, (height/2)+5, 150, 20)
        clearButton = returnButtonBuilder.build()
        val confirmButtonBuilder = ColoredButtonWidget.builder(Text.translatable("screen.fabricvision.confirm")) {
            parent.clear(true)
            client?.setScreen(null)
        }
        confirmButtonBuilder.color(0xFF0000)
        confirmButtonBuilder.dimensions((width/2)+1, (height/2)+5, 150, 20)
        deleteButton = confirmButtonBuilder.build()

        addDrawableChild(clearButton)
        addDrawableChild(deleteButton)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        val setMrlText = Text.translatable("screen.fabricvision.confirm_deletion")
        context.drawText(textRenderer, setMrlText, (width/2)-(textRenderer.getWidth(setMrlText)/2), (height/2)-20, 0xFFFFFF, false)
    }

    override fun shouldPause() = false


}