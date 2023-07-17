package io.github.lucaargolo.fabricvision.common.recipe

import io.github.lucaargolo.fabricvision.utils.RegistryCompendium
import net.minecraft.recipe.RecipeType
import net.minecraft.registry.Registries

object RecipeTypeCompendium: RegistryCompendium<RecipeType<*>>(Registries.RECIPE_TYPE) {

}