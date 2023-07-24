package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(at = @At("HEAD"), method = "updateTargetedEntity", cancellable = true)
    public void cancelProjectorEntityUpdate(float tickDelta, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }
    
    @Inject(at = @At("HEAD"), method = "getFov", cancellable = true)
    public void getProjectorFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            cir.setReturnValue(90.0);
        }
    }

    //TODO: Cancel nausea



}
