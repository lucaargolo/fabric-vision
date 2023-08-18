package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.CameraHelper;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "isUsingSpyglass", cancellable = true)
    public void fabricVision_injectIsUsingCamera(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(CameraHelper.isUsingCamera(player)) {
            cir.setReturnValue(true);
        }
    }

}
