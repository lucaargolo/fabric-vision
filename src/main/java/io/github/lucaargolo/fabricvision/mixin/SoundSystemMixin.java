package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.player.callback.MinecraftAudioCallback;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/Channel;close()V"), method = "stopAll")
    public void stopAudioCallbackChannel(CallbackInfo ci) {
        MinecraftAudioCallback.Companion.getSoundChannel().close();
    }


}
