package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.block.BlockCompendium
import io.github.lucaargolo.fabricvision.common.block.ProjectorBlock
import io.github.lucaargolo.fabricvision.common.blockentity.BookshelfSpeakerBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.RotationPropertyHelper

class BookshelfSpeakerBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<BookshelfSpeakerBlockEntity> {

    override fun render(entity: BookshelfSpeakerBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()

        matrices.push()

        val yaw = RotationPropertyHelper.toDegrees(entity.cachedState[ProjectorBlock.ROTATION]) +180f
        matrices.translate(0.5, 0.0, 0.5)
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw))
        matrices.translate(-0.5, 0.0, -0.5)
        val model = client.bakedModelManager.blockModels.getModel(BlockCompendium.BOOKSHELF_SPEAKER.defaultState)
        client.blockRenderManager.modelRenderer.render(matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getEntitySolid(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)), null, model, 1f, 1f, 1f, light, overlay)

        matrices.pop()
    }

}