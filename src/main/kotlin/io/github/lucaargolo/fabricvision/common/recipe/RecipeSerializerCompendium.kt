package io.github.lucaargolo.fabricvision.common.recipe

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries

object RecipeSerializerCompendium: RegistryCompendium<RecipeSerializer<*>>(Registries.RECIPE_SERIALIZER) {

}