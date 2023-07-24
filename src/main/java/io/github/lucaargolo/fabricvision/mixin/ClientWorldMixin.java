package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    @Inject(at = @At("HEAD"), method = "runQueuedChunkUpdates", cancellable = true)
    public void aaaa(CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }

}
