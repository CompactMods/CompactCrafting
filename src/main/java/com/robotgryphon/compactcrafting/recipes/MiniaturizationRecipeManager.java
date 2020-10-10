package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MiniaturizationRecipeManager {
    private Map<ResourceLocation, MiniaturizationRecipe> recipes;

    public static MiniaturizationRecipeManager INSTANCE = new MiniaturizationRecipeManager();

    private MiniaturizationRecipeManager() {
        this.recipes = new HashMap<>();
    }

    public static void clear() {
        INSTANCE.recipes.clear();
    }

    public static void add(ResourceLocation rl, MiniaturizationRecipe recipe) {
        INSTANCE.recipes.putIfAbsent(rl, recipe);
    }

    public static Collection<MiniaturizationRecipe> getAll() {
        return INSTANCE.recipes.values();
    }
}
