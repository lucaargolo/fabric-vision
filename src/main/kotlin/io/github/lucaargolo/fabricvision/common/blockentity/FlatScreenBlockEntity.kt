package io.github.lucaargolo.fabricvision.common.blockentity

import io.github.lucaargolo.fabricvision.FabricVision
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.lwjgl.system.MemoryUtil
import sun.misc.Unsafe
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer

class FlatScreenBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntityCompendium.FLAT_SCREEN, pos, state) {

    var identifier: Identifier? = null
        get() {
            if(field == null) {
                initializeClient()
            }
            return field
        }

    var mediaPlayer: EmbeddedMediaPlayer? = null
    var videoSurface: VideoSurface? = null

    var image: NativeImage? = null
    var texture: NativeImageBackedTexture? = null


    fun initializeClient() {
        if(mediaPlayer == null) {
            val mediaPlayerFactory = MediaPlayerFactory()
            mediaPlayer = mediaPlayerFactory.mediaPlayers()?.newEmbeddedMediaPlayer()


            val bufferFormatCallback = object: BufferFormatCallback {

                override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
                    return BufferFormat("RGBA", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
                }

                override fun allocatedBuffers(buffers: Array<out ByteBuffer>?) {
                    println("cu")
                }

            }

            val renderCallback = object: RenderCallback {
                override fun display(mediaPlayer: MediaPlayer, nativeBuffers: Array<out ByteBuffer>, bufferFormat: BufferFormat) {
                    nativeBuffers.forEach {
                        image?.let { image ->
                            it.rewind()
                            image.pointer = FabricVision.addressField.getLong(it)
                            texture?.upload()
                        }
                    }

                }

            }

            videoSurface = mediaPlayerFactory.videoSurfaces()?.newVideoSurface(bufferFormatCallback, renderCallback, true)

            mediaPlayer?.videoSurface()?.set(videoSurface)

            mediaPlayer?.media()?.startPaused("C:\\Users\\Luca\\Downloads\\video.mp4")

            mediaPlayer?.video()?.videoDimension()?.let {
                image = NativeImage(NativeImage.Format.RGBA, it.width, it.height+10, true)
                texture = NativeImageBackedTexture(image)
                identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", texture)
            }

        }else{
            mediaPlayer?.controls()?.pause()
        }

    }


}