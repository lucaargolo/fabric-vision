package io.github.lucaargolo.fabricvision.client.render.item

import io.github.lucaargolo.fabricvision.common.item.DiskItem
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Arm
import net.minecraft.util.math.RotationAxis

class DiskDynamicItemRenderer: DynamicItemRenderer {

    override fun render(stack: ItemStack, mode: ModelTransformationMode, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()

        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)

        val leftHanded = client.options.mainArm.value == Arm.LEFT
        val type = (stack.item as? DiskItem)?.type ?: Type.NONE
        val realType = stack.nbt?.let { if(it.contains("stream") && it.getBoolean("stream")) Type.STREAM else type } ?: type
        val model = client.bakedModelManager.getModel(realType.model)
        client.itemRenderer.renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model)

        if(mode != ModelTransformationMode.GUI) {
            matrices.push()
            model.transformation.getTransformation(mode).apply(leftHanded, matrices)

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180f))
            matrices.translate(0.0, 0.2, -0.016)
            matrices.scale(0.005f, 0.005f, -0.005f)
            val entry = matrices.peek()
            val matrix = entry.positionMatrix
            val name = stack.nbt?.let { if(it.contains("name")) it.getString("name") else "" } ?: ""
            val showName = if(name.isEmpty()) Text.translatable("tooltip.fabricvision.no_name") else Text.literal(name)

            client.textRenderer.wrapLines(showName, 70).forEachIndexed { index, orderedText ->
                val width = client.textRenderer.getWidth(orderedText)
                client.textRenderer.draw(orderedText, -width/2f, 10f*index, 0xFFFFFF, true, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x000000, light)
            }
            matrices.pop()
        }

        matrices.pop()
    }

}