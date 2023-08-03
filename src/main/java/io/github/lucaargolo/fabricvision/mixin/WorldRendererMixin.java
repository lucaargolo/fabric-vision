package io.github.lucaargolo.fabricvision.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lucaargolo.fabricvision.client.FabricVisionClient;
import io.github.lucaargolo.fabricvision.mixed.WorldRendererMixed;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
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

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererMixed {


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

    @Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V", ordinal = 0), method = "render")
    public void renderClientPlayerInProjector(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if(FabricVisionClient.INSTANCE.isRenderingProjector()) {
            Vec3d vec3d = camera.getPos();
            double d = vec3d.getX();
            double e = vec3d.getY();
            double f = vec3d.getZ();
            VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
            renderEntity(client.player, d, e, f, tickDelta, matrices, immediate);
        }
    }


}
