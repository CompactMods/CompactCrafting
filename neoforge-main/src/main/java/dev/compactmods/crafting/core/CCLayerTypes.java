package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.SimpleLayerType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CCLayerTypes {

    public static final ResourceLocation REC_LAYERS = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_layers");

    public static DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create(REC_LAYERS, CompactCrafting.MOD_ID);

    public static Registry<RecipeLayerType<?>> RECIPE_LAYER_TYPES = RECIPE_LAYERS.makeRegistry(b -> {});

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final DeferredHolder<RecipeLayerType<?>, RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", SimpleLayerType.supplier(FilledComponentRecipeLayer.CODEC));

    public static final DeferredHolder<RecipeLayerType<?>, RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", SimpleLayerType.supplier(HollowComponentRecipeLayer.CODEC));

    public static final DeferredHolder<RecipeLayerType<?>, RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", SimpleLayerType.supplier(MixedComponentRecipeLayer.CODEC));

    public static final DeferredHolder<RecipeLayerType<?>, RecipeLayerType<EmptyRecipeLayer>> EMPTY_LAYER_TYPE =
            RECIPE_LAYERS.register("empty", SimpleLayerType.supplier(EmptyRecipeLayer.CODEC));

    // endregion ======================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPE_LAYERS.register(eventBus);
    }
}
