package com.robotgryphon.compactcrafting.recipes.layers.dim;

import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayerMatcher;

public interface IDynamicRecipeLayerMatcher<T extends IDynamicRecipeLayer> extends IRecipeLayerMatcher<T> {
    @Override
    default int getPriority() {
        return 10; // Prioritize dynamic recipe layers by default
    }
}
