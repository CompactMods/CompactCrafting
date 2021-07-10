package com.robotgryphon.compactcrafting.recipes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponent;
import com.robotgryphon.compactcrafting.api.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.api.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.api.layers.dim.IFixedSizedRecipeLayer;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponentTypeCodec;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerTypeCodec;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class MiniaturizationRecipeCodec implements Codec<MiniaturizationRecipe> {

    private static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerTypeCodec.INSTANCE.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    private static final Codec<IRecipeComponent> COMPONENT_CODEC =
            RecipeComponentTypeCodec.INSTANCE.dispatchStable(IRecipeComponent::getType, RecipeComponentType::getCodec);

    MiniaturizationRecipeCodec() {}

    @Override
    public <T> DataResult<Pair<MiniaturizationRecipe, T>> decode(DynamicOps<T> ops, T input) {
        int recipeSize = Codec.INT.optionalFieldOf("recipeSize", -1)
                .codec()
                .parse(ops, input)
                .result().get();

        ItemStack catalyst = ItemStack.CODEC.fieldOf("catalyst").codec()
                .parse(ops, input)
                .resultOrPartial(CompactCrafting.RECIPE_LOGGER::error)
                .orElse(ItemStack.EMPTY);

        List<IRecipeLayer> layers = LAYER_CODEC.listOf().fieldOf("layers").codec()
                .parse(ops, input)
                .resultOrPartial(CompactCrafting.RECIPE_LOGGER::error)
                .get();

        List<ItemStack> outputs = ItemStack.CODEC.listOf().fieldOf("outputs").codec()
                .parse(ops, input)
                .resultOrPartial(CompactCrafting.RECIPE_LOGGER::error)
                .get();

        Map<String, IRecipeComponent> components = Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC).fieldOf("components")
                .codec()
                .parse(ops, input)
                .resultOrPartial(CompactCrafting.RECIPE_LOGGER::error)
                .get();

        boolean hasFixedLayers = layers.stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);

        // if we don't have a fixed size layer to base dimensions off of, and the recipe size won't fit in a field
        if (!hasFixedLayers && !FieldProjectionSize.canFitDimensions(recipeSize)) {
            MiniaturizationRecipe partial = new MiniaturizationRecipe(layers, catalyst, outputs, components);
            return DataResult.error(
                    "Specified recipe size will not fit in a crafting field: " + recipeSize,
                    Pair.of(partial, input));
        }

        MiniaturizationRecipe recipe = new MiniaturizationRecipe(recipeSize, layers, catalyst, outputs, components);
        recipe.recalculateDimensions();

        return DataResult.success(Pair.of(recipe, input));
    }

    @Override
    public <T> DataResult<T> encode(MiniaturizationRecipe input, DynamicOps<T> ops, T prefix) {

        DataResult<T> layers = LAYER_CODEC.listOf().encodeStart(ops, input.getLayerListForCodecWrite());

        DataResult<T> components = Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC)
                .encodeStart(ops, input.getComponents().getAllComponents());

        DataResult<T> catalyst = ItemStack.CODEC.encodeStart(ops, input.getCatalyst());
        DataResult<T> outputs = ItemStack.CODEC.listOf()
                .encodeStart(ops, ImmutableList.copyOf(input.getOutputs()));

        RecordBuilder<T> builder = ops.mapBuilder();

        builder.add("type", Codec.STRING.encodeStart(ops, "compactcrafting:miniaturization"));

        if(input.hasSpecifiedSize())
            builder.add("recipeSize", Codec.INT.encodeStart(ops, input.getSize()));

        return builder.add("layers", layers)
                .add("components", components)
                .add("catalyst", catalyst)
                .add("outputs", outputs)
                .build(prefix);
    }
}
