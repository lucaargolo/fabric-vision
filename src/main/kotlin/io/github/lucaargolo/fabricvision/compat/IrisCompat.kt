package io.github.lucaargolo.fabricvision.compat

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.render.WorldRenderer

interface IrisCompat {

    fun isRenderingShadowPass(): Boolean

    fun setupProjectorWorldRender()

    fun startProjectorWorldRender(worldRenderer: WorldRenderer)

    fun endProjectorWorldRender(worldRenderer: WorldRenderer)

    companion object {
        val INSTANCE: IrisCompat by lazy {
            if(FabricLoader.getInstance().isModLoaded("iris")) {
                IrisCompatImpl()
            }else{
                object: IrisCompat {
                    override fun isRenderingShadowPass() = false

                    override fun setupProjectorWorldRender() {}

                    override fun startProjectorWorldRender(worldRenderer: WorldRenderer) {}

                    override fun endProjectorWorldRender(worldRenderer: WorldRenderer) {}
                }
            }
        }
    }

}