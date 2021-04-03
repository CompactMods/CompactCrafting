package com.robotgryphon.compactcrafting.recipes.setup;

import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class BaseRecipeType<T extends RecipeBase> implements IRecipeType<RecipeBase> {
    private final ResourceLocation registryName;

    public BaseRecipeType(ResourceLocation location) {
        this.registryName = location;
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public void register() {
        Registry.register(Registry.RECIPE_TYPE, registryName, this);
    }
}
