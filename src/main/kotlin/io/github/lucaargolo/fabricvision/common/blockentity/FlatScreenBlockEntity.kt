package io.github.lucaargolo.fabricvision.common.blockentity

import com.mojang.blaze3d.platform.TextureUtil
import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.FabricVision
import io.github.lucaargolo.fabricvision.utils.MediaPlayerHolder
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.lwjgl.system.jni.JNINativeInterface
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import java.nio.ByteBuffer
import java.util.*

class FlatScreenBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BlockEntityCompendium.FLAT_SCREEN, pos, state) {

    var uuid = UUID.randomUUID()
    var initialized = false

    var mediaPlayer: MediaPlayer? = null
    var texture: NativeImageBackedTexture? = null
    var identifier = Identifier("missigno")

    fun createMediaPlayer() {
        val formatCallback = object: BufferFormatCallback {
            override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
                if(sourceWidth != texture?.image?.width || sourceHeight != texture?.image?.height) texture?.let{ texture ->
                    println("Creating new image (${sourceWidth}x${sourceHeight}) for ${uuid}.")
                    texture.image = NativeImage(NativeImage.Format.RGBA, sourceWidth, sourceHeight, true)
                    RenderSystem.recordRenderCall {
                        TextureUtil.prepareImage(texture.glId, sourceWidth, sourceHeight)
                    }
                }
                return BufferFormat("RGBA", sourceWidth, sourceHeight, intArrayOf(sourceWidth * 4), intArrayOf(sourceHeight))
            }
            override fun allocatedBuffers(buffers: Array<ByteBuffer>) {}
        }

        val renderCallback = RenderCallback { _, buffers, _ ->
            if (buffers.size == 1) texture?.image?.let { image ->
                image.pointer = JNINativeInterface.GetDirectBufferAddress(buffers[0])
                texture?.upload()
            }
        }

        MediaPlayerHolder.addPlayer(uuid, formatCallback, renderCallback) {
            mediaPlayer = it
            if(!initialized) {
                initialized = true
                play()
            }
        }
    }

    fun initialize() {
        if(texture == null) {
            texture = NativeImageBackedTexture(1, 1, true)
            identifier = MinecraftClient.getInstance().textureManager.registerDynamicTexture("video", texture)
        }

        if(mediaPlayer == null || !MediaPlayerHolder.isAlive(uuid)) {
            createMediaPlayer()
        }
    }

    fun play() {
        if(mediaPlayer == null || !MediaPlayerHolder.isAlive(uuid)) {
            createMediaPlayer()
        }else{
            mediaPlayer?.submit {
                if(mediaPlayer?.media()?.isValid == true) {
                    mediaPlayer?.controls()?.pause()
                }else{
                    mediaPlayer?.media()?.startPaused("C:\\Users\\Luca\\Downloads\\video5.mp4")
                }
            }
        }
    }

    override fun markRemoved() {
        super.markRemoved()
        if(world?.isClient == true) {
            MediaPlayerHolder.cleanPlayer(uuid)
        }
    }

    companion object {

        fun clientTick(world: World, pos: BlockPos, state: BlockState, blockEntity: FlatScreenBlockEntity) {
            MediaPlayerHolder.keepAlive(blockEntity.uuid)
        }

    }


}