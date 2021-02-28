package com.robotgryphon.compactcrafting.recipes.layers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RecipeLayerComponentLookup {
    public Map<BlockPos, String> components;

    public static final Codec<RecipeLayerComponentLookup> CODEC = new PrimitiveCodec<RecipeLayerComponentLookup>() {
        @Override
        public <T> DataResult<RecipeLayerComponentLookup> read(DynamicOps<T> ops, T input) {
            return Codec.STRING.listOf().listOf().decode(ops, input).flatMap(s -> {
                List<List<String>> first = s.getFirst();

                RecipeLayerComponentLookup lookup = new RecipeLayerComponentLookup();

                // TODO IMPL

                return DataResult.success(lookup);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, RecipeLayerComponentLookup value) {
            return null;
        }
    };

    public RecipeLayerComponentLookup() {
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
}
