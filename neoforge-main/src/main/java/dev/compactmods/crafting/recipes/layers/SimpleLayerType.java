package dev.compactmods.crafting.recipes.layers;

import com.mojang.serialization.MapCodec;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;

import java.util.function.Supplier;

public class SimpleLayerType<T extends IRecipeLayer> implements RecipeLayerType<T> {

    private final MapCodec<T> codec;

    private SimpleLayerType(MapCodec<T> codec) {
        this.codec = codec;
    }

    public static <L extends IRecipeLayer> SimpleLayerType<L> of(MapCodec<L> codec) {
        return new SimpleLayerType<>(codec);
    }

    public static <L extends IRecipeLayer> Supplier<SimpleLayerType<L>> supplier(MapCodec<L> codec) {
        return () -> of(codec);
    }

    @Override
    public MapCodec<T> getCodec() {
        return this.codec;
    }
}