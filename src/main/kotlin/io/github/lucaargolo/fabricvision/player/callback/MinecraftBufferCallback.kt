package io.github.lucaargolo.fabricvision.player.callback

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import java.nio.ByteBuffer

class MinecraftBufferCallback(private val mmp: MinecraftMediaPlayer): BufferFormatCallback {

    override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
        if(mmp.texture == null) {
            println("Creating texture ${mmp.uuid}")
            RenderSystem.recordRenderCall {
                mmp.texture = NativeImageBackedTexture(1, 1, true)
                mmp.identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", mmp.texture)
                if(sourceWidth != mmp.texture?.image?.width || sourceHeight != mmp.texture?.image?.height) mmp.texture?.let{ texture ->
                    println("Updating image ${mmp.uuid} (${sourceWidth}x${sourceHeight})")
                    texture.image = NativeImage(NativeImage.Format.RGBA, sourceWidth, sourceHeight, true)
                    TextureUtil.prepareImage(texture.glId, sourceWidth, sourceHeight)
                }
            }
        }

        return BufferFormat("RGBA", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
    }

    override fun allocatedBuffers(buffers: Array<ByteBuffer>) {

    }

}