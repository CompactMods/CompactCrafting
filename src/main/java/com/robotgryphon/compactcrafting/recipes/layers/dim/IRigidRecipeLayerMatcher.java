package com.robotgryphon.compactcrafting.recipes.layers.dim;

import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayerMatcher;

public interface IRigidRecipeLayerMatcher<T extends IRigidRecipeLayer> extends IRecipeLayerMatcher<T> {
    @Override
    default int getPriority() {
        return -10; // Lower the priority by default for rigid recipe layers
    }
}
