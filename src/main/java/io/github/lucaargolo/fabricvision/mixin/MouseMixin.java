package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.CameraHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), method = "updateMouse")
    public void fabricVision_fixDigitalCameraMouseSmoothness(Args args) {
        PlayerEntity player = client.player;
        if(player != null && client.options.getPerspective().isFirstPerson() && CameraHelper.isUsingCamera(player)) {
            float fov = CameraHelper.INSTANCE.getDigitalCameraFovMultiplier();

            double m = (70.0*MathHelper.clamp(fov, 0.1, 1.0) + 2.0)/9.0;

            double k = args.get(0);
            double l = args.get(1);

            args.set(0, k*m);
            args.set(1, l*m);
        }
    }

}
