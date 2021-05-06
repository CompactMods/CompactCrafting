package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponent;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayerMatcher;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerComponentPositionLookup;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IRigidRecipeLayerMatcher;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class MixedComponentRecipeLayer implements IRigidRecipeLayer {
    private AxisAlignedBB dimensions;
    private RecipeLayerComponentPositionLookup componentLookup;

    public static final Codec<MixedComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            RecipeLayerComponentPositionLookup.CODEC
                    .fieldOf("pattern")
                    .forGetter(MixedComponentRecipeLayer::getComponentLookup)
    ).apply(i, MixedComponentRecipeLayer::new));

    public MixedComponentRecipeLayer() {
        this(AxisAlignedBB.ofSize(0, 0, 0));
        this.componentLookup = new RecipeLayerComponentPositionLookup();
    }

    public MixedComponentRecipeLayer(AxisAlignedBB dimensions) {
        this.dimensions = dimensions;
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
        recalculateDimensions();
    }

    public void addMultiple(String component, Collection<BlockPos> locations) {
        boolean recalc = false;
        for(BlockPos loc : locations) {
            if(!componentLookup.containsLocation(loc)) {
                componentLookup.add(loc, component);
                recalc = true;
            }
        }

        if (recalc)
            recalculateDimensions();
    }

    private void recalculateDimensions() {
        this.dimensions = BlockSpaceUtil.getBoundsForBlocks(componentLookup.getAllPositions());
    }

    public AxisAlignedBB getDimensions() {
        return this.dimensions;
    }

    @Override
    public Map<String, Integer> getComponentTotals(Map<String, ? extends RecipeComponent> componentMap) {
        return componentLookup.getComponentTotals(componentMap);
    }

    @Override
    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        return componentLookup.getRequiredComponentKeyForPosition(pos);
    }

    @Override
    public Collection<BlockPos> getPositionsForComponent(String component) {
        return componentLookup.getPositionsForComponent(component);
    }

    @Override
    public Collection<BlockPos> getFilledPositions() {
        return componentLookup.getFilledPositions();
    }

    @Override
    public boolean isPositionFilled(BlockPos pos) {
        return componentLookup.isPositionFilled(pos);
    }

    public int getNumberFilledPositions(Map<String, ? extends RecipeComponent> componentMap) {
        return getComponentTotals(componentMap)
                .values()
                .stream()
                .reduce(0, Integer::sum);
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.MIXED_LAYER_TYPE.get();
    }

    public static class Matcher extends ForgeRegistryEntry<IRecipeLayerMatcher<?>> implements IRigidRecipeLayerMatcher<MixedComponentRecipeLayer> {
        public MixedComponentRecipeLayer getMatch(Map<BlockPos, String> compMap, AxisAlignedBB recipeDimensions) {
            // Add air blocks explicitly
            int y = compMap.keySet().iterator().next().getY();
            int xSize = (int) recipeDimensions.getXsize();
            int zSize = (int) recipeDimensions.getZsize();
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    compMap.putIfAbsent(new BlockPos(x, y, z), "_");
                }
            }

            MixedComponentRecipeLayer layer = new MixedComponentRecipeLayer();
            layer.componentLookup.addAll(compMap);
            layer.recalculateDimensions();
            return layer;
        }
    }
}
