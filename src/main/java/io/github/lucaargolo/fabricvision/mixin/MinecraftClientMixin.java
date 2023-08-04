package io.github.lucaargolo.fabricvision.mixin;


import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.client.ProjectorProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "getFramebuffer", cancellable = true)
    public void getProjectorFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ProjectorProgram program = FabricVisionClient.INSTANCE.getRenderingProjector();
            if(program != null) {
                cir.setReturnValue(program.getFramebuffer());
            }
        }
    }


}
