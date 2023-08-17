package io.github.lucaargolo.fabricvision.mixin;

import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.client.render.screen.MediaPlayerScreen;
import io.github.lucaargolo.fabricvision.common.item.DigitalCameraItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    public void fabricVision_cancelHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if((player != null && DigitalCameraItem.Companion.isUsingCamera(player)) || client.currentScreen instanceof MediaPlayerScreen) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    public void fabricVision_renderDigitalCameraOverlay(DrawContext context, float scale, CallbackInfo ci) {
        PlayerEntity player = client.player;
        if(player != null && DigitalCameraItem.Companion.isUsingCamera(player)) {
            FabricVisionClient.INSTANCE.renderDigitalCameraHud(context, getTextRenderer(), scaledWidth, scaledHeight, scale);
            ci.cancel();
        }
    }

}
