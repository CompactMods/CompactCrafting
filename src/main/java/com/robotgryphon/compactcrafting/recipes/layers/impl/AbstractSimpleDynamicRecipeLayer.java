package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.robotgryphon.compactcrafting.recipes.components.RecipeComponent;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * An abstract base class for a simple (one component key) dynamic recipe layer
 */
public abstract class AbstractSimpleDynamicRecipeLayer implements IDynamicRecipeLayer {
    protected String componentKey;
    protected AxisAlignedBB recipeDimensions;
    protected Collection<BlockPos> filledPositions;

    protected AbstractSimpleDynamicRecipeLayer(String componentKey) {
        this.componentKey = componentKey;
    }

    @Override
    public Map<String, Integer> getComponentTotals(Map<String, ? extends RecipeComponent> componentMap) {
        return Collections.singletonMap(componentKey, getNumberFilledPositions(componentMap));
    }

    @Override
    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        return Optional.ofNullable(componentKey);
    }

    @Override
    public Collection<BlockPos> getPositionsForComponent(String component) {
        return this.componentKey.equals(component) ? this.filledPositions : Collections.emptySet();
    }

    @Override
    public Collection<BlockPos> getFilledPositions() {
        return this.filledPositions;
    }

    @Override
    public boolean isPositionFilled(BlockPos pos) {
        return this.filledPositions.contains(pos);
    }

    @Override
    public int getNumberFilledPositions(Map<String, ? extends RecipeComponent> componentMap) {
        return filledPositions.size();
    }

    protected abstract Collection<BlockPos> recalculatePositions();

    @Override
    public void setRecipeDimensions(AxisAlignedBB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    @Override
    public void recalculateRequirements() {
        this.filledPositions = recalculatePositions();
    }

    public String getComponentKey() {
        return componentKey;
    }

    public void setComponentKey(String component) {
        this.componentKey = component;
    }
}
