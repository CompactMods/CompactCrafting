package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.SimpleLayerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static dev.compactmods.crafting.recipes.components.ComponentRegistration.c;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CCLayerTypes {

    public static final ResourceLocation REC_LAYERS = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_layers");

    public static DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create(REC_LAYERS, CompactCrafting.MOD_ID);

    public static IForgeRegistry<RecipeLayerType<?>> RECIPE_LAYER_TYPES;

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", FilledComponentRecipeLayer::new);

    public static final RegistryObject<RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", HollowComponentRecipeLayer::new);

    public static final RegistryObject<RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", MixedComponentRecipeLayer::new);

    public static final RegistryObject<RecipeLayerType<EmptyRecipeLayer>> EMPTY_LAYER_TYPE =
            RECIPE_LAYERS.register("empty", SimpleLayerType.supplier(EmptyRecipeLayer.CODEC));

    // endregion ======================================================================================================

    public static void init(IEventBus eventBus) {
        RECIPE_LAYERS.register(eventBus);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void newRegistries(final NewRegistryEvent evt) {
        final var b = new RegistryBuilder<RecipeComponentType<?>>()
                .setName(REC_LAYERS)
                .setType(c(RecipeLayerType.class));

        evt.create(b);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void layerRegistration(RegistryEvent.Register<RecipeLayerType<?>> evt) {
        RECIPE_LAYER_TYPES = evt.getRegistry();
    }
}
