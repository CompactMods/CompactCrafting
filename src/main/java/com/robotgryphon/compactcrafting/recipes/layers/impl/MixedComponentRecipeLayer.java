package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerComponentLookup;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayer;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class MixedComponentRecipeLayer extends RecipeLayer implements IRigidRecipeLayer {
    private AxisAlignedBB dimensions;
    private RecipeLayerComponentLookup componentLookup;
    private Map<String, Integer> totalCache;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            RecipeLayerComponentLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this.dimensions = AxisAlignedBB.withSizeAtOrigin(0, 0, 0);
        this.componentLookup = new RecipeLayerComponentLookup();
    }

    public MixedComponentRecipeLayer(RecipeLayerComponentLookup components) {
        this.componentLookup = components;
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public RecipeLayerComponentLookup getComponentLookup() {
        return this.componentLookup;
    }

    public void add(String component, BlockPos location) {
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

    public Map<String, Integer> getComponentTotals() {
        if(this.totalCache != null)
            return this.totalCache;

        Map<String, Integer> totals = new HashMap<>();
        componentLookup.components.forEach((pos, comp) -> {
            int prev = 0;
            if(!totals.containsKey(comp))
                totals.put(comp, 0);
            else
                prev = totals.get(comp);

            totals.replace(comp, prev + 1);
        });

        this.totalCache = totals;

        return this.totalCache;
    }

    public String getRequiredComponentKeyForPosition(BlockPos pos) {
        if(componentLookup.components.containsKey(pos))
            return componentLookup.components.get(pos);

        return null;
    }

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @param component
     * @return
     */
    public Collection<BlockPos> getPositionsForComponent(String component) {
        if(component == null)
            return Collections.emptySet();

        return componentLookup.components.entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getValue(), component))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Collection<BlockPos> getNonAirPositions() {
        return componentLookup.components.keySet();
    }

    public boolean isPositionRequired(BlockPos pos) {
        return componentLookup.components.containsKey(pos);
    }

    public int getNumberFilledPositions() {
        return getComponentTotals()
                .values()
                .stream()
                .reduce(0, Integer::sum);
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.MIXED_LAYER_TYPE.get();
    }
}
