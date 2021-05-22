package com.robotgryphon.compactcrafting.recipes.layers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.api.layers.dim.IDynamicSizedRecipeLayer;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class HollowComponentRecipeLayer implements IRecipeLayer, IDynamicSizedRecipeLayer {

    private String componentKey;
    private AxisAlignedBB recipeDimensions;
    private Collection<BlockPos> filledPositions;

    public static final Codec<HollowComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("wall").forGetter(HollowComponentRecipeLayer::getComponent)
    ).apply(i, HollowComponentRecipeLayer::new));

    public HollowComponentRecipeLayer(String component) {
        this.componentKey = component;
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.HOLLOW_LAYER_TYPE.get();
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
        return filledPositions;
    }

    public Collection<BlockPos> getFilledPositions() {
        return this.filledPositions;
    }

    public boolean isPositionFilled(BlockPos pos) {
        return true;
    }

    public int getNumberFilledPositions() {
        return filledPositions.size();
    }

    public void setComponent(String component) {
        this.componentKey = component;
    }

    @Override
    public void setRecipeDimensions(AxisAlignedBB dimensions) {
        this.recipeDimensions = dimensions;
        this.recalculateRequirements();
    }

    /**
     * Used to recalculate dynamic-sized recipe layers. Expected to be called
     * any time components or base recipe dimensions change.
     */
    @Override
    public void recalculateRequirements() {
        this.filledPositions = getWallPositions();
    }

    public Collection<BlockPos> getWallPositions() {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, recipeDimensions.getXsize() - 1, 0, recipeDimensions.getZsize() - 1);
        AxisAlignedBB insideBounds = layerBounds.move(1, 0, 1).contract(2, 0, 2);

        Set<BlockPos> positions = BlockPos.betweenClosedStream(layerBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Set<BlockPos> inside = BlockPos.betweenClosedStream(insideBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        positions.removeAll(inside);
        return positions;
    }

    public String getComponent() {
        return this.componentKey;
    }
}
