package com.robotgryphon.compactcrafting.recipes.layers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponent;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
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
            AxisAlignedBB boundsForBlocks = BlockSpaceUtil.getBoundsForBlocks(value.getAllPositions());

            // Use TreeMaps for sorting ints naturally from lowest to highest
            Map<Integer, Map<Integer, String>> revMap = new TreeMap<>();

            value.components.forEach((pos, comp) -> {
                Map<Integer, String> zMap = revMap.computeIfAbsent(pos.getX(), x -> new TreeMap<>());
                zMap.put(pos.getZ(), comp);
            });

            List<List<String>> fin = new ArrayList<>(revMap.size());
            for (Map<Integer, String> xMap : revMap.values()) {
                fin.add(new ArrayList<>(xMap.values()));
            }

            DataResult<T> encoded = Codec.STRING.listOf().listOf().encode(fin, ops, ops.empty());

            return encoded
                    .resultOrPartial(err -> CompactCrafting.LOGGER.error("Failed to encode layer component position lookup: {}", err))
                    .get();
        }
    };

    public RecipeLayerComponentPositionLookup() {
        this.components = new HashMap<>();
    }

    public void add(BlockPos location, String component) {
        components.putIfAbsent(location, component);
    }

    public void addAll(Map<BlockPos, String> compMap) {
        components.putAll(compMap);
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

    public Map<String, Integer> getComponentTotals(Map<String, ? extends RecipeComponent> componentMap) {
        if (this.totalCache == null) {
            this.totalCache = components
                    .entrySet()
                    .stream()
                    .filter(e -> componentMap.containsKey(e.getValue())) // Filter out invalid components like "_" for air
                    .collect(Collectors.toMap(Map.Entry::getValue, e -> 1, Integer::sum));
        }

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
