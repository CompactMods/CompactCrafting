package dev.compactmods.crafting.api.recipe;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationRecipe {
    ICatalystMatcher getCatalyst();

    ItemStack[] getOutputs();

    ResourceLocation getRecipeIdentifier();

    int getCraftingTime();

    AABB getDimensions();

    Optional<IRecipeLayer> getLayer(int layer);

    IRecipeComponents getComponents();

    Stream<IRecipeLayer> getLayers();
}
