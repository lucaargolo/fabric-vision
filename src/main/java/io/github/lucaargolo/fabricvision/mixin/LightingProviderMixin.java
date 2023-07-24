package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightingProvider.class)
public class LightingProviderMixin {

    @Inject(at = @At("HEAD"), method = "doLightUpdates", cancellable = true)
    public void aaaa(CallbackInfoReturnable<Integer> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.isOnThread() && FabricVisionClient.INSTANCE.isRenderingProjector()) {
            cir.setReturnValue(0);
        }
    }


}
