package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.SimpleLayerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CCLayerTypes {

    public static final ResourceLocation REC_LAYERS = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_layers");

    public static DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create(REC_LAYERS, CompactCrafting.MOD_ID);

    public static Supplier<IForgeRegistry<RecipeLayerType<?>>> RECIPE_LAYER_TYPES = RECIPE_LAYERS.makeRegistry(() -> new RegistryBuilder<RecipeLayerType<?>>()
            .setName(REC_LAYERS));

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", SimpleLayerType.supplier(FilledComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", SimpleLayerType.supplier(HollowComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", SimpleLayerType.supplier(MixedComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<EmptyRecipeLayer>> EMPTY_LAYER_TYPE =
            RECIPE_LAYERS.register("empty", SimpleLayerType.supplier(EmptyRecipeLayer.CODEC));

    // endregion ======================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPE_LAYERS.register(eventBus);
    }
}
