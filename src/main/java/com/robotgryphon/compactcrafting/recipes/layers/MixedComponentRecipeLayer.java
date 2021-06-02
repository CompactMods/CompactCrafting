package com.robotgryphon.compactcrafting.recipes.layers;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.api.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.api.layers.dim.IFixedSizedRecipeLayer;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MixedComponentRecipeLayer implements IRecipeLayer, IFixedSizedRecipeLayer {
    private AxisAlignedBB dimensions;
    private RecipeLayerComponentPositionLookup componentLookup;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            RecipeLayerComponentPositionLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this.dimensions = AxisAlignedBB.ofSize(0, 0, 0);
        this.componentLookup = new RecipeLayerComponentPositionLookup();
    }

    public MixedComponentRecipeLayer(RecipeLayerComponentPositionLookup components) {
        this.componentLookup = components;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public RecipeLayerComponentPositionLookup getComponentLookup() {
        return this.componentLookup;
    }

    public void addComponentAtLocation(String component, BlockPos location) {
        componentLookup.add(location, component);
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public void addMultiple(String component, Collection<BlockPos> locations) {
        boolean recalc = false;
        for(BlockPos loc : locations) {
            if(!componentLookup.containsLocation(loc)) {
                componentLookup.add(loc, component);
                recalc = true;
            }
        }

        if(recalc)
            this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    @Override
    public Set<String> getRequiredComponents() {
        return ImmutableSet.copyOf(componentLookup.getComponents());
    }

    public Map<String, Integer> getComponentTotals() {
        return componentLookup.getComponentTotals();
    }

    /**
     * Gets a component key for the given (normalized) position.
     *
     * @param pos
     * @return
     */
    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        return componentLookup.getRequiredComponentKeyForPosition(pos);
    }

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    public Collection<BlockPos> getPositionsForComponent(String component) {
        return componentLookup.getPositionsForComponent(component);
    }

    /**
     * Gets a set of non-air positions that are required for the layer to match.
     * This is expected to trim the air positions off the edges and return the positions with NW
     * in the 0, 0 position.
     *
     * @return
     */
    public Collection<BlockPos> getFilledPositions() {
        return componentLookup.getFilledPositions();
    }

    public boolean isPositionFilled(BlockPos pos) {
        return componentLookup.isPositionFilled(pos);
    }

    public int getNumberFilledPositions() {
        return getComponentTotals()
                .values()
                .stream()
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean matches(IRecipeLayerBlocks blocks) {
        return false;
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.MIXED_LAYER_TYPE.get();
    }
}
