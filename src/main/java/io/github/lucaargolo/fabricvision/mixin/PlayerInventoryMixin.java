package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.common.item.DigitalCameraItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;

    @Inject(at = @At("HEAD"), method = "scrollInHotbar", cancellable = true)
    public void fabricVision_captureScrollForDigitalCamera(double scrollAmount, CallbackInfo ci) {
        if(MinecraftClient.getInstance().options.getPerspective().isFirstPerson() && DigitalCameraItem.Companion.isUsingCamera(player)) {
            float fov = FabricVisionClient.INSTANCE.getDigitalCameraFovMultiplier();
            fov = fov + Math.signum((float) scrollAmount) * -0.1f;
            fov = MathHelper.clamp(fov, 0.1f, 1.33333f);
            FabricVisionClient.INSTANCE.setDigitalCameraFovMultiplier(fov);
            ci.cancel();
        }
    }


}
