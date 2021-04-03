package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FilledComponentRecipeLayer implements IRecipeLayer, IDynamicRecipeLayer {

    private String componentKey;
    private AxisAlignedBB recipeDimensions;

    public static final Codec<FilledComponentRecipeLayer> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("component").forGetter(FilledComponentRecipeLayer::getComponent)
        ).apply(in, FilledComponentRecipeLayer::new));

    public FilledComponentRecipeLayer(String component) {
        this.componentKey = component;
    }

    public String getComponent() {
        return this.componentKey;
    }

    public Map<String, Integer> getComponentTotals() {
        return Collections.singletonMap(componentKey, getNumberFilledPositions());
    }

    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        return Optional.ofNullable(componentKey);
    }

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    public Collection<BlockPos> getPositionsForComponent(String component) {
        if (component == this.componentKey)
            return getFilledPositions();

        return Collections.emptySet();
    }

    /**
     * Gets a set of non-air positions that are required for the layer to match.
     * This is expected to trim the air positions off the edges and return the positions with NW
     * in the 0, 0 position.
     *
     * @return
     */
    public Collection<BlockPos> getFilledPositions() {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, recipeDimensions.getXsize() - 1, 1, recipeDimensions.getZsize() - 1);
        return BlockPos.betweenClosedStream(layerBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    public boolean isPositionFilled(BlockPos pos) {
        return true;
    }

    public int getNumberFilledPositions() {
        return (int) Math.ceil(recipeDimensions.getXsize() * recipeDimensions.getZsize());
    }

    public RecipeLayerType<?> getType() {
        return Registration.FILLED_LAYER_SERIALIZER.get();
    }

    public void setComponent(String component) {
        this.componentKey = component;
    }

    /**
     * Used to update a recipe layer to change the size of the recipe base.
     *
     * @param dimensions The new dimensions of the recipe.
     */
    public void setRecipeDimensions(AxisAlignedBB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    public void recalculateRequirements() {

    }
}
