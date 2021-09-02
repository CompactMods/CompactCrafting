package dev.compactmods.compactcrafting.api.recipe;

import java.util.Optional;
import dev.compactmods.compactcrafting.api.components.IRecipeComponents;
import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public interface IMiniaturizationRecipe {
    ItemStack getCatalyst();

    ItemStack[] getOutputs();

    ResourceLocation getRecipeIdentifier();

    int getCraftingTime();

    AxisAlignedBB getDimensions();

    Optional<IRecipeLayer> getLayer(int layer);

    IRecipeComponents getComponents();
}
