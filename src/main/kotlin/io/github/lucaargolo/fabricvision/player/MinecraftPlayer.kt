package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.common.item.DiskItem.Type
import io.github.lucaargolo.fabricvision.FabricVision
import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.UUID

interface MinecraftPlayer {

    val uuid: UUID
    val type: Type
    val status: Status

    var mrl: String
    var pos: Vec3d
    var stream: Boolean
    var playing: Boolean
    var repeating: Boolean
    var rate: Float
    var startTime: Long
    var forceTime: Boolean
    var volume: Float
    var audioMaxDist: Float
    var audioRefDist: Float

    fun getTexture(tickDelta: Float): Identifier {
        return TRANSPARENT
    }

    fun tick()

    fun updateDuration(durationConsumer: (Long) -> Unit)

    fun updateTitle(titleConsumer: (String) -> Unit)

    fun close()

    enum class Status(val interactable: Boolean, val acceptMedia: Boolean, val formatting: Formatting) {
        NO_PLAYER(false, false, Formatting.RED),    // mmp not created, used for screen
        WAITING(false, false, Formatting.RED),      // mmp just created, waiting for liberation
        CREATING(false, false, Formatting.RED),     // mp requested
        NO_MEDIA(false, true, Formatting.YELLOW),   // mp ready, no media
        LOADING(false, false, Formatting.YELLOW),   // mp ready, media requested
        LOADED(false, true, Formatting.YELLOW),     // mp ready, media loaded
        PLAYING(true, true, Formatting.GREEN),      // mp ready, media playing
        PAUSED(true, true, Formatting.GREEN),       // mp ready, media paused
        STOPPED(true, true, Formatting.GREEN),      // mp ready, media stopped
        CLOSING(false, false, Formatting.RED),      // mp closing
        CLOSED(false, false, Formatting.RED);       // mp closed

        val translationKey: String
            get() = "tooltip.${FabricVision.MOD_ID}.status.${name.lowercase()}"

        val descriptionKey: String
            get() = "$translationKey.description"

    }

    companion object {
        val TRANSPARENT = ModIdentifier("textures/gui/transparent.png")
    }

}