package io.github.lucaargolo.fabricvision.compat

import net.coderbot.iris.Iris
import net.coderbot.iris.block_rendering.BlockRenderingSettings
import net.coderbot.iris.pipeline.PipelineManager
import net.coderbot.iris.pipeline.WorldRenderingPipeline
import net.coderbot.iris.shadows.ShadowRenderingState
import net.coderbot.iris.uniforms.CapturedRenderingState
import net.coderbot.iris.uniforms.SystemTimeUniforms
import net.coderbot.iris.uniforms.SystemTimeUniforms.FrameCounter
import net.minecraft.client.render.WorldRenderer
import org.joml.Matrix4f
import java.util.*

class IrisCompatImpl: IrisCompat {

    private val emptyPipeline = EmptyRenderingPipeline()
    private val worldPipelineField = WorldRenderer::class.java.getDeclaredField("pipeline").also { it.isAccessible = true }
    private val irisPipelineField = PipelineManager::class.java.getDeclaredField("pipeline").also { it.isAccessible = true }

    private var worldPipelineBackup: WorldRenderingPipeline? = null
    private var irisPipelineBackup: WorldRenderingPipeline? = null

    private var gbufferModelViewBackup: Matrix4f? = null
    private var gbufferProjectionBackup: Matrix4f? = null

    private val counterCountField = FrameCounter::class.java.getDeclaredField("count").also { it.isAccessible = true }
    private var counterCountBackup: Int = 0

    private var timerFrameTimeCounterField = SystemTimeUniforms.Timer::class.java.getDeclaredField("frameTimeCounter").also { it.isAccessible = true }
    private var timerFrameTimeCounterBackup = 0f
    private var timerLastFrameTimeField = SystemTimeUniforms.Timer::class.java.getDeclaredField("lastFrameTime").also { it.isAccessible = true }
    private var timerLastFrameTimeBackup = 0f
    private var timerLastStartTimeField = SystemTimeUniforms.Timer::class.java.getDeclaredField("lastStartTime").also { it.isAccessible = true }
    private var timerLastStartTimeBackup: OptionalLong? = null

    private var extendedVertexFormatBackup: Boolean = false

    override fun setupProjectorWorldRender() {
        gbufferModelViewBackup = CapturedRenderingState.INSTANCE.gbufferModelView
        gbufferProjectionBackup = CapturedRenderingState.INSTANCE.gbufferProjection
        counterCountBackup = counterCountField.get(SystemTimeUniforms.COUNTER) as Int
        timerFrameTimeCounterBackup = timerFrameTimeCounterField.get(SystemTimeUniforms.TIMER) as Float
        timerLastFrameTimeBackup = timerLastFrameTimeField.get(SystemTimeUniforms.TIMER) as Float
        timerLastStartTimeBackup = timerLastStartTimeField.get(SystemTimeUniforms.TIMER) as OptionalLong?
    }
    override fun startProjectorWorldRender(worldRenderer: WorldRenderer) {
        worldPipelineBackup = worldPipelineField.get(worldRenderer) as WorldRenderingPipeline?
        worldPipelineField.set(worldRenderer, emptyPipeline)
        val pipelineManager = Iris.getPipelineManager()
        irisPipelineBackup = irisPipelineField.get(pipelineManager) as WorldRenderingPipeline?
        irisPipelineField.set(pipelineManager, null)
        extendedVertexFormatBackup = BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()

        BlockRenderingSettings.INSTANCE.setUseExtendedVertexFormat(false)
    }

    override fun endProjectorWorldRender(worldRenderer: WorldRenderer) {
        CapturedRenderingState.INSTANCE.gbufferModelView = gbufferModelViewBackup
        CapturedRenderingState.INSTANCE.gbufferProjection = gbufferProjectionBackup
        counterCountField.set(SystemTimeUniforms.COUNTER, counterCountBackup)
        timerFrameTimeCounterField.set(SystemTimeUniforms.TIMER, timerFrameTimeCounterBackup)
        timerLastFrameTimeField.set(SystemTimeUniforms.TIMER, timerLastFrameTimeBackup)
        timerLastStartTimeField.set(SystemTimeUniforms.TIMER, timerLastStartTimeBackup)

        worldPipelineField.set(worldRenderer, worldPipelineBackup)
        val pipelineManager = Iris.getPipelineManager()
        irisPipelineField.set(pipelineManager, irisPipelineBackup)

        BlockRenderingSettings.INSTANCE.setUseExtendedVertexFormat(extendedVertexFormatBackup)
    }

    override fun isRenderingShadowPass(): Boolean {
        return ShadowRenderingState.areShadowsCurrentlyBeingRendered()
    }


}