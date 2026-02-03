package dev.compactmods.crafting.api.recipe;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.setup.FakeInventory;
import dev.compactmods.crafting.api.recipe.setup.RecipeBase;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.stream.Stream;

public interface IMiniaturizationRecipe extends Recipe<FakeInventory>, RecipeBase {

    Identifier RECIPE_TYPE_ID = CompactCrafting.identifier("miniaturization_recipe");

    ItemPredicate catalystMatcher();

    ItemStack[] getOutputs();

    int getCraftingTime();

    AABB getDimensions();

    Optional<IRecipeLayer> getLayer(int layer);

    IRecipeComponents getComponents();

    Stream<IRecipeLayer> getLayers();
}
