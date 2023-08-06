package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void cancelHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        if(client.currentScreen instanceof MediaPlayerScreen) {
            ci.cancel();
        }
    }

}
