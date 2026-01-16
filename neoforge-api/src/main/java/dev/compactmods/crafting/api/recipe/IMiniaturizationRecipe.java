package dev.compactmods.crafting.api.recipe;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.setup.FakeInventory;
import dev.compactmods.crafting.api.recipe.setup.RecipeBase;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationRecipe extends Recipe<FakeInventory>, RecipeBase {
    ItemPredicate catalystTest();

    ItemStack[] getOutputs();

    int getCraftingTime();

    AABB getDimensions();

    Optional<IRecipeLayer> getLayer(int layer);

    IRecipeComponents getComponents();

    Stream<IRecipeLayer> getLayers();
}
