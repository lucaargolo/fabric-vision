package io.github.lucaargolo.fabricvision.mixin;


import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.client.HandHelper;
import io.github.lucaargolo.fabricvision.client.ProjectorProgram;
import io.github.lucaargolo.fabricvision.client.CameraHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final public GameOptions options;

    @Shadow @Final private Window window;

    @Shadow @Final public static boolean IS_SYSTEM_MAC;

    @Inject(at = @At("HEAD"), method = "getFramebuffer", cancellable = true)
    public void fabricVision_injectProjectorFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ProjectorProgram program = FabricVisionClient.INSTANCE.getRenderingProjector();
            if(program != null) {
                cir.setReturnValue(program.getFramebuffer());
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0, shift = At.Shift.BEFORE), method = "handleInputEvents", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void fabricVision_takeDigitalCameraPicture(CallbackInfo ci, boolean bl3) {
        if(player != null && CameraHelper.isUsingCamera(player)) {
            while(this.options.attackKey.wasPressed()) {
                CameraHelper.INSTANCE.setHudHiddenBackup(this.options.hudHidden);
                this.options.hudHidden = true;
                CameraHelper.INSTANCE.setTakePicture(5);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;resize(IIZ)V"), method = "onResolutionChanged", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void fabricVision_changeHandFramebufferSize(CallbackInfo ci, int i, Framebuffer framebuffer) {
        HandHelper.INSTANCE.getHandSolidFramebuffer().resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);
        HandHelper.INSTANCE.getHandTranslucentFramebuffer().resize(this.window.getFramebufferWidth(), this.window.getFramebufferHeight(), IS_SYSTEM_MAC);
    }


}
