package io.github.lucaargolo.fabricvision.mixin;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.client.HandHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Unique
    private int fabricVision_previousHandFramebuffer = -1;

    @Inject(at = @At("HEAD"), method = "renderFirstPersonItem")
    public void fabricVision_renderHandItemHead(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(HandHelper.INSTANCE.getRenderingHand()) {
            fabricVision_previousHandFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            boolean isSolid = true;
            if(item.getItem() instanceof BlockItem blockItem) {
                isSolid = RenderLayers.getBlockLayer(blockItem.getBlock().getDefaultState()) != RenderLayer.getTranslucent();
            }
            if(isSolid) {
                HandHelper.INSTANCE.getHandSolidFramebuffer().beginWrite(false);
            }else{
                HandHelper.INSTANCE.getHandTranslucentFramebuffer().beginWrite(false);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "renderFirstPersonItem")
    public void fabricVision_renderHandItemTail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(fabricVision_previousHandFramebuffer != -1) {
            ((VertexConsumerProvider.Immediate) vertexConsumers).draw();
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fabricVision_previousHandFramebuffer);
            fabricVision_previousHandFramebuffer = -1;
        }
    }

}
