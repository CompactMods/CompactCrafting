package dev.compactmods.crafting.recipes.components;

import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public interface RecipeComponents {

    DeferredHolder<RecipeComponentType<?>, RecipeComponentType<BlockComponent>> BLOCK_COMPONENT =
            CompactCraftingCommon.RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType<>(BlockComponent.CODEC));

    DeferredHolder<RecipeComponentType<?>, RecipeComponentType<EmptyBlockComponent>> EMPTY_BLOCK_COMPONENT =
            CompactCraftingCommon.RECIPE_COMPONENTS.register("empty", () -> new SimpleRecipeComponentType<>(EmptyBlockComponent.CODEC));

    static void prepare() {

    }
}
