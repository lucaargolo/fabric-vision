package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.FabricVision
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class MinecraftMediaSoundInstance(private val volumeSupplier: () -> Float): SoundInstance {

    private val sound = Sound("${FabricVision.MOD_ID}:libvlc_sound", { 1f }, { 1f }, 1, Sound.RegistrationType.SOUND_EVENT, true, false, 0)

    override fun getAttenuationType() = SoundInstance.AttenuationType.LINEAR

    override fun getRepeatDelay() = 0

    override fun isRepeatable() = false

    override fun isRelative() = true

    override fun getVolume() = volumeSupplier.invoke()

    override fun getPitch() = 1f

    override fun getX() = 0.0

    override fun getY() = 0.0

    override fun getZ() = 0.0

    override fun getId(): Identifier = sound.identifier

    override fun getCategory() = SoundCategory.BLOCKS

    override fun getSoundSet(soundManager: SoundManager) = null

    override fun getSound() = sound

}