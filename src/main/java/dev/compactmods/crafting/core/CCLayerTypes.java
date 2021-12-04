package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCLayerTypes {

    @SuppressWarnings("unchecked")
    public static DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create((Class) RecipeLayerType.class, CompactCrafting.MOD_ID);

    public static IForgeRegistry<RecipeLayerType<?>> RECIPE_LAYER_TYPES;

    static {
        RECIPE_LAYERS.makeRegistry("recipe_layers", () -> new RegistryBuilder<RecipeLayerType<?>>()
                .tagFolder("recipe_layers"));
    }

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", FilledComponentRecipeLayer::new);

    public static final RegistryObject<RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", HollowComponentRecipeLayer::new);

    public static final RegistryObject<RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", MixedComponentRecipeLayer::new);

    // endregion ======================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPE_LAYERS.register(eventBus);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void layerRegistration(RegistryEvent.Register<RecipeLayerType<?>> evt) {
        RECIPE_LAYER_TYPES = evt.getRegistry();
    }
}
