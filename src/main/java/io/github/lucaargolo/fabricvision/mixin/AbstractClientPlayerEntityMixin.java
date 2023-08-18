package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.CameraHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "getFovMultiplier", cancellable = true)
    public void fabricVision_injectDigitalCameraFovMultiplier(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && CameraHelper.isUsingCamera(player)) {
            cir.setReturnValue(CameraHelper.INSTANCE.getDigitalCameraFovMultiplier());
        }
    }


}
