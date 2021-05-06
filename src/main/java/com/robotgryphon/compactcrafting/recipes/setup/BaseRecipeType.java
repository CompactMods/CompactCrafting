package com.robotgryphon.compactcrafting.recipes.setup;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;

public class BaseRecipeType<T extends RecipeBase> implements IRecipeType<RecipeBase> {
    private final ResourceLocation registryName;

    public BaseRecipeType(RegistryObject<IRecipeSerializer<T>> regObject) {
        this.registryName = regObject.getId();
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public void register() {
        Registry.register(Registry.RECIPE_TYPE, registryName, this);
    }
}
