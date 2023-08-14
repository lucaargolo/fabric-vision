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
        mmp.nativeTexture?.close()
        RenderSystem.recordRenderCall {
            mmp.nativeTexture = NativeImageBackedTexture(1, 1, true)
            mmp.texture = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", mmp.nativeTexture)
            if(sourceWidth != mmp.nativeTexture?.image?.width || sourceHeight != mmp.nativeTexture?.image?.height) mmp.nativeTexture?.let{ texture ->
                texture.image = NativeImage(NativeImage.Format.RGBA, sourceWidth, sourceHeight, true)
                TextureUtil.prepareImage(texture.glId, sourceWidth, sourceHeight)
            }
        }
        return BufferFormat("RGBA", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
    }

    override fun allocatedBuffers(buffers: Array<ByteBuffer>) {

    }

}