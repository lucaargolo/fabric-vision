package io.github.lucaargolo.fabricvision.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.mixed.WorldRendererMixed;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements WorldRendererMixed {


    @Shadow @Final private MinecraftClient client;

    @Shadow private Frustum frustum;
    @Shadow private boolean shouldCaptureFrustum;
    @Shadow private @Nullable Frustum capturedFrustum;
    @Shadow @Final private Vector4f[] capturedFrustumOrientation;
    @Shadow @Final private Vector3d capturedFrustumPosition;

    @Shadow
    public BufferBuilderStorage bufferBuilders;
    private Frustum fabricVision_frustum;
    private boolean fabricVision_shouldCaptureFrustum;
    @Nullable
    private Frustum fabricVision_capturedFrustum;
    private final Vector4f[] fabricVision_capturedFrustumOrientation = new Vector4f[8];
    private final Vector3d fabricVision_capturedFrustumPosition = new Vector3d();

    private BufferBuilderStorage fabricVision_bufferBuilders;


    @Override
    public void backup() {
        fabricVision_frustum = this.frustum;
        fabricVision_shouldCaptureFrustum = this.shouldCaptureFrustum;
        fabricVision_capturedFrustum = this.capturedFrustum;
        System.arraycopy(capturedFrustumOrientation, 0, fabricVision_capturedFrustumOrientation, 0, capturedFrustumOrientation.length);
        fabricVision_capturedFrustumPosition.x = capturedFrustumPosition.x;
        fabricVision_capturedFrustumPosition.y = capturedFrustumPosition.y;
        fabricVision_capturedFrustumPosition.z = capturedFrustumPosition.z;
        fabricVision_bufferBuilders = bufferBuilders;
    }

    @Override
    public void restore() {
        this.frustum = fabricVision_frustum;
        this.shouldCaptureFrustum = fabricVision_shouldCaptureFrustum;
        this.capturedFrustum = fabricVision_capturedFrustum;
        System.arraycopy(fabricVision_capturedFrustumOrientation, 0, capturedFrustumOrientation, 0, capturedFrustumOrientation.length);
        capturedFrustumPosition.x = fabricVision_capturedFrustumPosition.x;
        capturedFrustumPosition.y = fabricVision_capturedFrustumPosition.y;
        capturedFrustumPosition.z = fabricVision_capturedFrustumPosition.z;
        bufferBuilders = fabricVision_bufferBuilders;
    }

    @Inject(at = @At("HEAD"), method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "setupTerrain", cancellable = true)
    public void setupTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "updateChunks", cancellable = true)
    public void updateChunks(Camera camera, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "canDrawEntityOutlines", cancellable = true)
    protected void canDrawEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            cir.setReturnValue(false);
        }
    }


}
