package io.github.lucaargolo.fabricvision.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.data.TrackedData
import kotlin.reflect.KProperty

class TrackedDataDelegate<D: Any>(val trackedData: TrackedData<D>) {
    operator fun <E: Entity> getValue(entity: E, property: KProperty<*>): D {
        return entity.dataTracker.get(trackedData)
    }

    operator fun <E: Entity> setValue(entity: E, property: KProperty<*>, value: D) {
        entity.dataTracker.set(trackedData, value)
    }

}