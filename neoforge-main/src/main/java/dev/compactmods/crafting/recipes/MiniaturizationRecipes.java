package dev.compactmods.crafting.recipes;

import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.SimpleLayerType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static dev.compactmods.crafting.CompactCraftingCommon.RECIPES;
import static dev.compactmods.crafting.CompactCraftingCommon.RECIPE_LAYERS;
import static dev.compactmods.crafting.CompactCraftingCommon.RECIPE_TYPES;

public interface MiniaturizationRecipes {

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    DeferredHolder<RecipeLayerType<?>, RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", SimpleLayerType.supplier(FilledComponentRecipeLayer.CODEC));

    DeferredHolder<RecipeLayerType<?>, RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", SimpleLayerType.supplier(HollowComponentRecipeLayer.CODEC));

    DeferredHolder<RecipeLayerType<?>, RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", SimpleLayerType.supplier(MixedComponentRecipeLayer.CODEC));

    DeferredHolder<RecipeLayerType<?>, RecipeLayerType<EmptyRecipeLayer>> EMPTY_LAYER_TYPE =
            RECIPE_LAYERS.register("empty", SimpleLayerType.supplier(EmptyRecipeLayer.CODEC));

    // endregion ======================================================================================================

    DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER =
            RECIPES.register("miniaturization", () -> new RecipeSerializer<>(MiniaturizationRecipe.CODEC, MiniaturizationRecipe.STREAM_CODEC));

    DeferredHolder<RecipeType<?>, RecipeType<MiniaturizationRecipe>> MINIATURIZATION_RECIPE = RECIPE_TYPES.register(IMiniaturizationRecipe.RECIPE_TYPE_ID.getPath(),
            () -> RecipeType.simple(IMiniaturizationRecipe.RECIPE_TYPE_ID));

    static void prepare() {

    }
}
