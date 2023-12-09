package dev.compactmods.crafting.recipes.components;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ComponentRegistration {

    public static final ResourceLocation RECIPE_COMPONENTS_ID = new ResourceLocation(CompactCrafting.MOD_ID, "recipe_components");

    public static DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS = DeferredRegister.create(RECIPE_COMPONENTS_ID, CompactCrafting.MOD_ID);

    public static Registry<RecipeComponentType<?>> COMPONENTS = RECIPE_COMPONENTS.makeRegistry(b -> {});

    // ================================================================================================================
    //   RECIPE COMPONENTS
    // ================================================================================================================
    public static final DeferredHolder<RecipeComponentType<?>, RecipeComponentType<BlockComponent>> BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType<>(BlockComponent.CODEC));

    public static final DeferredHolder<RecipeComponentType<?>, RecipeComponentType<EmptyBlockComponent>> EMPTY_BLOCK_COMPONENT =
            RECIPE_COMPONENTS.register("empty", () -> new SimpleRecipeComponentType<>(EmptyBlockComponent.CODEC));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================

    public static void init(IEventBus modBus) {
        RECIPE_COMPONENTS.register(modBus);
    }
}
