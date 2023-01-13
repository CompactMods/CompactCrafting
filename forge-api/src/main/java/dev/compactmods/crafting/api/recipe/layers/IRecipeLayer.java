package dev.compactmods.crafting.api.recipe.layers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import net.minecraft.core.BlockPos;

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

    Stream<BlockPos> getPositionsForComponent(String component);

    default boolean requiresAllBlocksIdentified() {
        return true;
    }

    default boolean matches(IRecipeComponents components, IRecipeBlocks blocks) {
        return !requiresAllBlocksIdentified() || blocks.allIdentified();
    }

    RecipeLayerType<?> getType();

    /**
     * @deprecated No longer called by the recipe loader. Recipes will pull required component keys
     * from layer implementations, and automatically remap at the recipe level.
     */
    @Deprecated
    default void dropNonRequiredComponents(IRecipeComponents components) {
    }
}
