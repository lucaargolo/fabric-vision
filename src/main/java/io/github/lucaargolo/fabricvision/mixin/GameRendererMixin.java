package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "updateTargetedEntity", cancellable = true)
    public void cancelProjectorEntityUpdate(float tickDelta, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }
    
    @Inject(at = @At("HEAD"), method = "getFov", cancellable = true)
    public void getProjectorFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            cir.setReturnValue(30.0);
        }
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", shift = At.Shift.AFTER), method = "renderWorld", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void fixProjectorCamera(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci, boolean bl, Camera camera) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            Entity cameraEntity = this.client.getCameraEntity();
            if(cameraEntity != null) {
                camera.update(this.client.world, cameraEntity, false, false, tickDelta);
                camera.setPos(cameraEntity.getPos());
            }
        }
    }

    //TODO: Cancel nausea



}
