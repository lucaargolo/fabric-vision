package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.CameraHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
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
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.options.getPerspective().isFirstPerson() && CameraHelper.isUsingCamera(player)) {
            float oldFov = CameraHelper.INSTANCE.getDigitalCameraFovMultiplier();
            float newFov = MathHelper.clamp(oldFov + Math.signum((float) scrollAmount) * -0.1f, 0.1f, 1.33333f);
            if(oldFov != newFov) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 2.0f));
            }
            CameraHelper.INSTANCE.setDigitalCameraFovMultiplier(newFov);
            ci.cancel();
        }
    }


}
