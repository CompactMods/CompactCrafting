package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public static Optional<MiniaturizationRecipe> get(ResourceLocation rl) {
        if(!INSTANCE.recipes.containsKey(rl))
            return Optional.empty();

        return Optional.ofNullable(INSTANCE.recipes.get(rl));
    }

    public static Collection<MiniaturizationRecipe> getAll() {
        return INSTANCE.recipes.values();
    }
}
