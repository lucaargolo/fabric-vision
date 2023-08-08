package io.github.lucaargolo.fabricvision.common.sound

import io.github.lucaargolo.fabricvision.utils.ModIdentifier
import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object SoundCompendium: RegistryCompendium<SoundEvent>(Registries.SOUND_EVENT) {

    val DISK_INSERT = register("disk_insert")
    val DISK_EXTRACT = register("disk_extract")

    fun register(namespace: String): SoundEvent {
        val identifier = ModIdentifier(namespace)
        return register(identifier)
    }

    fun register(identifier: Identifier): SoundEvent {
        return register(identifier, SoundEvent.of(identifier))
    }

}