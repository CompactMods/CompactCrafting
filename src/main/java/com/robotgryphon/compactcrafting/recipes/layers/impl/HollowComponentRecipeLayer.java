package com.robotgryphon.compactcrafting.recipes.layers.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayerMatcher;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.dim.IDynamicRecipeLayerMatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HollowComponentRecipeLayer extends AbstractSimpleDynamicRecipeLayer {
    public static final Codec<HollowComponentRecipeLayer> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("wall").forGetter(HollowComponentRecipeLayer::getComponentKey)
    ).apply(i, HollowComponentRecipeLayer::new));

    public HollowComponentRecipeLayer(String componentKey) {
        super(componentKey);
    }

    protected Collection<BlockPos> recalculatePositions() {
        return getWallPositions(this.recipeDimensions);
    }

    private static Collection<BlockPos> getWallPositions(AxisAlignedBB recipeDimensions) {
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

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.HOLLOW_LAYER_TYPE.get();
    }

    public static class Matcher extends ForgeRegistryEntry<IRecipeLayerMatcher<?>> implements IDynamicRecipeLayerMatcher<HollowComponentRecipeLayer> {
        public HollowComponentRecipeLayer getMatch(Map<BlockPos, String> compMap, AxisAlignedBB recipeDimensions) {
            Set<String> componentKeys = new HashSet<>(compMap.values());
            if (compMap.keySet().equals(getWallPositions(recipeDimensions)) && componentKeys.size() == 1) {
                HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer(componentKeys.iterator().next());
                layer.setRecipeDimensions(recipeDimensions);
                return layer;
            }
            return null;
        }
    }
}
