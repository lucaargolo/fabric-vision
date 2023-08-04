package io.github.lucaargolo.fabricvision.compat

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps
import net.coderbot.iris.features.FeatureFlags
import net.coderbot.iris.gbuffer_overrides.matching.SpecialCondition
import net.coderbot.iris.gbuffer_overrides.state.RenderTargetStateListener
import net.coderbot.iris.gl.texture.TextureType
import net.coderbot.iris.helpers.Tri
import net.coderbot.iris.mixin.LevelRendererAccessor
import net.coderbot.iris.pipeline.SodiumTerrainPipeline
import net.coderbot.iris.pipeline.WorldRenderingPhase
import net.coderbot.iris.pipeline.WorldRenderingPipeline
import net.coderbot.iris.shaderpack.CloudSetting
import net.coderbot.iris.shaderpack.ParticleRenderingSettings
import net.coderbot.iris.shaderpack.texture.TextureStage
import net.coderbot.iris.uniforms.FrameUpdateNotifier
import net.minecraft.client.render.Camera
import java.util.*

class EmptyRenderingPipeline: WorldRenderingPipeline {

    override fun onShadowBufferChange() {
        
    }

    override fun beginLevelRendering() {
        
    }

    override fun renderShadows(p0: LevelRendererAccessor?, p1: Camera?) {
        
    }

    override fun addDebugText(p0: MutableList<String>?) {
        
    }

    override fun getForcedShadowRenderDistanceChunksForDisplay(): OptionalInt {
        return OptionalInt.empty()
    }

    override fun getTextureMap(): Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> {
        return Object2ObjectMaps.emptyMap()
    }

    override fun getPhase(): WorldRenderingPhase {
        return WorldRenderingPhase.NONE
    }

    override fun beginSodiumTerrainRendering() {
        
    }

    override fun endSodiumTerrainRendering() {
        
    }

    override fun setOverridePhase(p0: WorldRenderingPhase?) {
        
    }

    override fun setPhase(p0: WorldRenderingPhase?) {
        
    }

    override fun setSpecialCondition(p0: SpecialCondition?) {
        
    }

    override fun getRenderTargetStateListener(): RenderTargetStateListener {
        return RenderTargetStateListener.NOP
    }

    override fun getCurrentNormalTexture(): Int {
        return 0
    }

    override fun getCurrentSpecularTexture(): Int {
        return 0
    }

    override fun onSetShaderTexture(p0: Int) {
        
    }

    override fun beginHand() {
        
    }

    override fun beginTranslucents() {
        
    }

    override fun finalizeLevelRendering() {
        
    }

    override fun destroy() {
        
    }

    override fun getSodiumTerrainPipeline(): SodiumTerrainPipeline? {
        return null
    }

    override fun getFrameUpdateNotifier(): FrameUpdateNotifier? {
        return null
    }

    override fun shouldDisableVanillaEntityShadows(): Boolean {
        return false
    }

    override fun shouldDisableDirectionalShading(): Boolean {
        return false
    }

    override fun shouldDisableFrustumCulling(): Boolean {
        return false
    }

    override fun getCloudSetting(): CloudSetting {
        return CloudSetting.OFF
    }

    override fun shouldRenderUnderwaterOverlay(): Boolean {
        return false
    }

    override fun shouldRenderVignette(): Boolean {
        return false
    }

    override fun shouldRenderSun(): Boolean {
        return false
    }

    override fun shouldRenderMoon(): Boolean {
        return false
    }

    override fun shouldWriteRainAndSnowToDepthBuffer(): Boolean {
        return false
    }

    override fun getParticleRenderingSettings(): ParticleRenderingSettings {
        return ParticleRenderingSettings.BEFORE
    }

    override fun allowConcurrentCompute(): Boolean {
        return false
    }

    override fun hasFeature(p0: FeatureFlags?): Boolean {
        return false
    }

    override fun getSunPathRotation(): Float {
        return 0f
    }
}