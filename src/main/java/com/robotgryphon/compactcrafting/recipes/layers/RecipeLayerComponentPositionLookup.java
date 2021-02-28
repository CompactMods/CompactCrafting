package com.robotgryphon.compactcrafting.recipes.layers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeLayerComponentPositionLookup {
    private Map<BlockPos, String> components;
    private Map<String, Integer> totalCache;

    public static final Codec<RecipeLayerComponentPositionLookup> CODEC = new PrimitiveCodec<RecipeLayerComponentPositionLookup>() {
        @Override
        public <T> DataResult<RecipeLayerComponentPositionLookup> read(DynamicOps<T> ops, T input) {
            return Codec.STRING.listOf().listOf().decode(ops, input).flatMap(s -> {
                List<List<String>> layerList = s.getFirst();

                RecipeLayerComponentPositionLookup lookup = new RecipeLayerComponentPositionLookup();

                int zSize = layerList.size();

                String[][] mappedToArray = new String[zSize][];

                for(int z = 0; z < zSize; z++) {
                    List<String> layerComponents = layerList.get(z);
                    String[] xValues = new String[layerComponents.size()];
                    for(int x = 0; x < layerComponents.size(); x++) {
                        xValues[x] = layerComponents.get(x);
                    }

                    mappedToArray[z] = xValues;
                }

                lookup.components = RecipeHelper.convertMultiArrayToMap(mappedToArray);

                return DataResult.success(lookup);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, RecipeLayerComponentPositionLookup value) {
            return null;
        }
    };

    public RecipeLayerComponentPositionLookup() {
        this.components = new HashMap<>();
    }

    public void add(BlockPos location, String component) {
        components.putIfAbsent(location, component);
    }

    public Collection<String> getComponents() {
        return components.values();
    }

    public BlockPos[] getAllPositions() {
        return components.keySet().toArray(new BlockPos[0]);
    }

    public boolean containsLocation(BlockPos location) {
        return components.containsKey(location);
    }

    public Stream<Map.Entry<BlockPos, String>> stream() {
        return this.components.entrySet().stream();
    }

    public Map<String, Integer> getComponentTotals() {
        if(this.totalCache != null)
            return this.totalCache;

        Map<String, Integer> totals = new HashMap<>();
        components.forEach((pos, comp) -> {
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

    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        if(components.containsKey(pos))
            return Optional.ofNullable(components.get(pos));

        return Optional.empty();
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

        return components.entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getValue(), component))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Collection<BlockPos> getFilledPositions() {
        return components.keySet();
    }

    public boolean isPositionFilled(BlockPos pos) {
        return components.containsKey(pos);
    }
}
