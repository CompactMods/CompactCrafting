package dev.compactmods.compactcrafting.api.recipe;

import dev.compactmods.compactcrafting.api.components.IRecipeComponents;
import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Optional;

public interface IMiniaturizationRecipe {
    ItemStack getCatalyst();

    ItemStack[] getOutputs();

    ResourceLocation getId();

    int getCraftingTime();

    AxisAlignedBB getDimensions();

    Optional<IRecipeLayer> getLayer(int layer);

    IRecipeComponents getComponents();
}
