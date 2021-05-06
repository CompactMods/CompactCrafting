package com.robotgryphon.compactcrafting.recipes.layers;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;

/**
 * A matcher for an {@link IRecipeLayer}
 */
public interface IRecipeLayerMatcher<T extends IRecipeLayer> extends Comparable<IRecipeLayerMatcher<?>>, IForgeRegistryEntry<IRecipeLayerMatcher<?>> {
    /**
     * Get the layer matching priority of this layer. A higher number means a higher priority, and therefore will be attempted to be matched first. Defaults to 0.
     *
     * @return the layer matching priority of this layer.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns a possible match for the filled configuration of {@link BlockPos}.
     *
     * @param compMap A map of all filled relative {@link BlockPos} to their component key for a given layer. Never empty; always mutable.
     * @param recipeDimensions The dimensions of the filled blocks of the recipe.
     * @return A possible matched layer, or null if it doesn't match.
     */
    T getMatch(Map<BlockPos, String> compMap, AxisAlignedBB recipeDimensions);

    @Override
    default int compareTo(IRecipeLayerMatcher<?> other) {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}
