package dev.compactmods.crafting.recipes.layers;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class SimpleLayerType<T extends IRecipeLayer>
        extends ForgeRegistryEntry<RecipeLayerType<?>>
        implements RecipeLayerType<T> {

    private final Codec<T> codec;

    private SimpleLayerType(Codec<T> codec) {
        this.codec = codec;
    }

    public static <L extends IRecipeLayer> SimpleLayerType<L> of(Codec<L> codec) {
        return new SimpleLayerType<>(codec);
    }

    public static <L extends IRecipeLayer> Supplier<SimpleLayerType<L>> supplier(Codec<L> codec) {
        return () -> of(codec);
    }

    @Override
    public Codec<T> getCodec() {
        return this.codec;
    }
}
