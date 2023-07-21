package io.github.lucaargolo.fabricvision.player

import io.github.lucaargolo.fabricvision.FabricVision
import net.minecraft.client.sound.Sound
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundManager
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class MinecraftMediaSoundInstance(val pos: Vec3d): SoundInstance {

    private val sound = Sound("${FabricVision.MOD_ID}:libvlc_sound", { 1f }, { 1f }, 1, Sound.RegistrationType.SOUND_EVENT, true, false, 16)

    override fun getAttenuationType() = SoundInstance.AttenuationType.LINEAR

    override fun getRepeatDelay() = 0

    override fun isRepeatable() = false

    override fun isRelative() = true

    override fun getVolume() = 1f

    override fun getPitch() = 1f

    override fun getX() = pos.x

    override fun getY() = pos.y

    override fun getZ() = pos.z

    override fun getId(): Identifier = sound.identifier

    override fun getCategory() = SoundCategory.BLOCKS

    override fun getSoundSet(soundManager: SoundManager) = null

    override fun getSound() = sound

}