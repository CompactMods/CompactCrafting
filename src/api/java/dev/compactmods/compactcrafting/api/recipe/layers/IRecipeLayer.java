package dev.compactmods.compactcrafting.api.recipe.layers;

import dev.compactmods.compactcrafting.api.components.IRecipeComponents;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IRecipeLayer {

    Set<String> getComponents();

    Map<String, Integer> getComponentTotals();

    /**
     * Gets a component key for the given (normalized) position.
     *
     * @param pos
     * @return
     */
    Optional<String> getComponentForPosition(BlockPos pos);

    boolean matches(IRecipeComponents components, IRecipeLayerBlocks blocks);

    RecipeLayerType<?> getType();
}
