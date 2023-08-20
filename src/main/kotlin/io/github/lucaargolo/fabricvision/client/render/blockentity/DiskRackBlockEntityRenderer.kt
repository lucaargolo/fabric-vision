package io.github.lucaargolo.fabricvision.client.render.blockentity

import io.github.lucaargolo.fabricvision.common.block.DiskRackBlock
import io.github.lucaargolo.fabricvision.common.blockentity.DiskRackBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis

class DiskRackBlockEntityRenderer(private val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<DiskRackBlockEntity> {

    override fun render(entity: DiskRackBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val client = MinecraftClient.getInstance()
        val facing = entity.cachedState[DiskRackBlock.FACING]
        matrices.push()
        matrices.translate(0.5, 0.5, 0.5)
        matrices.multiply((if(facing.axis == Direction.Axis.Z) RotationAxis.POSITIVE_Y else RotationAxis.NEGATIVE_Y).rotationDegrees(facing.asRotation()))
        matrices.translate(0.0, -3.0/16.0, -7.0/16.0)
        matrices.scale(2f, 2f, 2f)
        entity.stacks.forEach { stack ->
            matrices.translate(0.0, 0.0, 0.5/16.0)
            client.itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.world, 0)
            matrices.translate(0.0, 0.0, 0.5/16.0)
        }
        matrices.pop()
    }

}