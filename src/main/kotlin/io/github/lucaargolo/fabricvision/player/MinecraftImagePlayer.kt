package io.github.lucaargolo.fabricvision.player

import com.mojang.blaze3d.systems.RenderSystem
import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.player.MinecraftPlayer.Status
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.jni.JNINativeInterface
import java.awt.image.BufferedImage
import java.net.MalformedURLException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.metadata.IIOMetadataNode
import kotlin.concurrent.thread
import kotlin.math.roundToInt


class MinecraftImagePlayer(override val uuid: UUID): MinecraftPlayer {

    override val type = Type.IMAGE
    override val status = Status.PAUSED

    @Volatile
    private var loading = false
    private var started = false
    private var lastMrl = ""
    override var mrl = ""

    override var pos = Vec3d.ZERO
    override var stream = false
    override var playing = false
    override var repeating = false
    override var rate = 1f
    override var startTime = 0L
    override var forceTime = false
    override var volume = 1f
    override var audioMaxDist = 0f
    override var audioRefDist = 0f

    private var internalTexture: Identifier? = null
    private var internalImage: NativeImageBackedTexture? = null
    private var internalBuffer: ByteBuffer? = null

    private var frameDelays = floatArrayOf()
    private var currentFrame = 0
    private var delay = 0

    override fun tick() {
        val client = MinecraftClient.getInstance()
        if(!loading) {
            if (!started || mrl != lastMrl) {
                started = true
                currentFrame = -1
                lastMrl = mrl
                internalImage?.let {
                    client.textureManager.destroyTexture(internalTexture)
                }
                internalImage = null
                internalTexture = null
                internalBuffer?.let {
                    MemoryUtil.memFree(internalBuffer)
                }
                internalBuffer = null
                loading = true
                thread {
                    try {
                        val input = ImageIO.createImageInputStream(URL(mrl).openStream())
                        val readers = ImageIO.getImageReaders(input)
                        if (readers.hasNext()) {
                            val reader = readers.next()
                            val param = reader.defaultReadParam
                            reader.setInput(input, false, false)
                            val frames = reader.getNumImages(true)
                            frameDelays = FloatArray(frames)
                            if (frames > 0) {
                                val width = reader.getWidth(0)
                                val height = reader.getHeight(0)
                                RenderSystem.recordRenderCall {
                                    internalImage = NativeImageBackedTexture(width, height, MinecraftClient.IS_SYSTEM_MAC)
                                }
                                internalBuffer = MemoryUtil.memAlloc(width * height * 4 * frames)
                                var lastBuffered = reader.read(0, param)
                                repeat(frames) { frame ->
                                    frameDelays[frame] = reader.getDelayTime(frame).toFloat()
                                    if (frame > 0 && reader.getWidth(frame) == width && reader.getHeight(frame) == height) {
                                        val buffered: BufferedImage = reader.read(frame, param)
                                        buffered.putABGRBuffer(internalBuffer, width * height * 4 * frame)
                                        lastBuffered = buffered
                                    } else {
                                        lastBuffered.putABGRBuffer(internalBuffer, width * height * 4 * frame)
                                    }
                                }
                            }
                            reader.dispose()
                            input.close()
                        }
                    } catch (ignored: MalformedURLException) {

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    loading = false
                }
            }
            if (internalTexture == null && internalImage != null) {
                internalTexture = client.textureManager.registerDynamicTexture("image", internalImage)
            }
        }
        //GIF Frame delays are in 100th of a second
        delay += 5
    }

    private fun ImageReader.getDelayTime(imageIndex: Int): Int {
        return try {
            val metadata = getImageMetadata(imageIndex)
            val format = metadata.nativeMetadataFormatName
            val root = metadata.getAsTree(format) as IIOMetadataNode
            val extension = root.getNode("GraphicControlExtension")
            val delayTime = extension.getAttribute("delayTime")
            delayTime.toInt()
        }catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun IIOMetadataNode.getNode(nodeName: String): IIOMetadataNode {
        for (i in 0 until length) {
            if (item(i).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                return item(i) as IIOMetadataNode
            }
        }
        val node = IIOMetadataNode(nodeName)
        appendChild(node)
        return node
    }

    override fun getTexture(tickDelta: Float): Identifier {
        val texture = internalImage ?: return MinecraftPlayer.TRANSPARENT
        val image = texture.image ?: return MinecraftPlayer.TRANSPARENT
        val buffer = internalBuffer ?: return MinecraftPlayer.TRANSPARENT

        val time = delay + (tickDelta * 5f)
        val totalTime = frameDelays.sum()

        val width = image.width
        val height = image.height

        val isValid = buffer.capacity() % (width * height * 4) == 0
        if (isValid) {
            val frames = buffer.capacity() / (width * height * 4)
            var frame = 0
            if(totalTime > 0) {
                var gifTime = time % totalTime

                while(gifTime > 0 && frame < frames-1) {
                    gifTime -= frameDelays.getOrNull(frame) ?: 0f
                    frame++
                }
            }
            if (frame != currentFrame) {
                currentFrame = frame
                val address = JNINativeInterface.GetDirectBufferAddress(buffer)
                MemoryUtil.memCopy(address + (width * height * 4L * frame), image.pointer, width * height * 4L)
                texture.upload()
            }
        }
        return internalTexture ?: MinecraftPlayer.TRANSPARENT
    }

    override fun updateDuration(durationConsumer: (Long) -> Unit) {
        durationConsumer.invoke(0L)
    }

    override fun updateTitle(titleConsumer: (String) -> Unit) {
        titleConsumer.invoke("")
    }

    override fun close() {
        val client = MinecraftClient.getInstance()
        internalImage?.let {
            client.textureManager.destroyTexture(internalTexture)
        }
        internalBuffer?.let {
            MemoryUtil.memFree(it)
        }
    }

    private fun BufferedImage.putABGRBuffer(byteBuffer: ByteBuffer?, offset: Int) {
        repeat(height) { y ->
            repeat(width) { x ->
                val argb = getRGB(x, y)
                val a = (argb shr 24) and 0xFF
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = (argb shr 0) and 0xFF
                val i = (width * y + x) * 4 + offset
                byteBuffer?.put(i + 0, r.toByte())
                byteBuffer?.put(i + 1, g.toByte())
                byteBuffer?.put(i + 2, b.toByte())
                byteBuffer?.put(i + 3, a.toByte())
            }
        }
    }

}