package io.github.lucaargolo.fabricvision.player.callback

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.player.MinecraftMediaPlayer
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.jni.JNINativeInterface
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer

class MinecraftRenderCallback(private val mmp: MinecraftMediaPlayer): RenderCallback {
    override fun display(player: MediaPlayer, buffers: Array<ByteBuffer>, bufferFormat: BufferFormat) {
        if (mmp.status.visible && buffers.size == 1) RenderSystem.recordRenderCall { mmp.texture?.image?.let { image ->
            val address = JNINativeInterface.GetDirectBufferAddress(buffers[0])
            MemoryUtil.memCopy(address, image.pointer, image.width * image.height * 4L)
            mmp.texture?.upload()
        } }
    }

}