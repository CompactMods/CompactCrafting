package dev.compactmods.crafting.api.recipe.layers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import net.minecraft.util.math.BlockPos;

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

    default boolean requiresAllBlocksIdentified() {
        return true;
    }

    default boolean matches(IRecipeComponents components, IRecipeLayerBlocks blocks) {
        return !requiresAllBlocksIdentified() || blocks.allIdentified();
    }

    RecipeLayerType<?> getType();
}
