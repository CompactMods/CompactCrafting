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

public class FilledComponentRecipeLayer extends AbstractSimpleDynamicRecipeLayer {
    public static final Codec<FilledComponentRecipeLayer> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("component").forGetter(FilledComponentRecipeLayer::getComponentKey)
    ).apply(in, FilledComponentRecipeLayer::new));

    public FilledComponentRecipeLayer(String componentKey) {
        super(componentKey);
    }

    @Override
    protected Collection<BlockPos> recalculatePositions() {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, recipeDimensions.getXsize() - 1, 0, recipeDimensions.getZsize() - 1);
        return BlockPos.betweenClosedStream(layerBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    @Override
    public RecipeLayerType<?> getType() {
        return Registration.FILLED_LAYER_TYPE.get();
    }

    public static class Matcher extends ForgeRegistryEntry<IRecipeLayerMatcher<?>> implements IDynamicRecipeLayerMatcher<FilledComponentRecipeLayer> {
        public FilledComponentRecipeLayer getMatch(Map<BlockPos, String> compMap, AxisAlignedBB recipeDimensions) {
            Set<String> componentKeys = new HashSet<>(compMap.values());
            int size = (int) (recipeDimensions.getXsize() * recipeDimensions.getZsize());
            if (compMap.size() == size && componentKeys.size() == 1) {
                FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer(componentKeys.iterator().next());
                layer.setRecipeDimensions(recipeDimensions);
                return layer;
            }
            return null;
        }
    }
}
