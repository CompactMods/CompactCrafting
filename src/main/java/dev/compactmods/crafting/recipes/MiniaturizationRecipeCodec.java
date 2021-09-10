package dev.compactmods.crafting.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.components.RecipeComponentTypeCodec;
import dev.compactmods.crafting.recipes.layers.RecipeLayerTypeCodec;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.item.ItemStack;

public class MiniaturizationRecipeCodec implements Codec<MiniaturizationRecipe> {

    public static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerTypeCodec.INSTANCE.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    public static final Codec<IRecipeComponent> COMPONENT_CODEC =
            RecipeComponentTypeCodec.INSTANCE.dispatchStable(IRecipeComponent::getType, RecipeComponentType::getCodec);

    MiniaturizationRecipeCodec() {
    }

    @Override
    public <T> DataResult<Pair<MiniaturizationRecipe, T>> decode(DynamicOps<T> ops, T input) {
        boolean debugOutput = ServerConfig.RECIPE_REGISTRATION.get();
        if (debugOutput) {
            CompactCrafting.RECIPE_LOGGER.debug("Starting recipe decode: {}", input.toString());
        }

        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        StringBuilder errorBuilder = new StringBuilder();

        int recipeSize = Codec.INT.optionalFieldOf("recipeSize", -1)
                .codec()
                .parse(ops, input)
                .result()
                .get();

        recipe.setRecipeSize(recipeSize);

        final DataResult<List<IRecipeLayer>> layers = LAYER_CODEC.listOf()
                .fieldOf("layers").codec()
                .parse(ops, input);

        if (layers.error().isPresent()) {
            final Optional<List<IRecipeLayer>> partialLayers = layers.resultOrPartial(errorBuilder::append);
            partialLayers.ifPresent(recipe::applyLayers);
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        final List<IRecipeLayer> layerList = layers
                .resultOrPartial(errorBuilder::append)
                .orElse(Collections.emptyList());

        recipe.applyLayers(layerList);

        boolean hasFixedLayers = layerList.stream().anyMatch(l -> l instanceof IFixedSizedRecipeLayer);
        if (debugOutput) {
            CompactCrafting.RECIPE_LOGGER.debug("Number of layers defined: {}", layerList.size());
            CompactCrafting.RECIPE_LOGGER.debug("Is fixed size: {}", hasFixedLayers);
        }

        // if we don't have a fixed size layer to base dimensions off of, and the recipe size won't fit in a field
        if (!hasFixedLayers && !MiniaturizationFieldSize.canFitDimensions(recipeSize)) {
            errorBuilder.append("Specified recipe size will not fit in a crafting field: ").append(recipeSize);
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        recipe.recalculateDimensions();

        ItemStack catalyst = ItemStack.CODEC.fieldOf("catalyst").codec()
                .parse(ops, input)
                .resultOrPartial(errorBuilder::append)
                .orElse(ItemStack.EMPTY);

        if (catalyst.isEmpty()) {
            CompactCrafting.LOGGER.warn("Warning: recipe has no catalyst; this may be unintentional.");
        }

        Optional<List<ItemStack>> outputs = ItemStack.CODEC.listOf().fieldOf("outputs").codec()
                .parse(ops, input)
                .resultOrPartial(errorBuilder::append);

        outputs.ifPresent(recipe::setOutputs);
        if (!outputs.isPresent()) {
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        if(recipe.getOutputs().length == 0) {
            errorBuilder.append("No outputs were defined.");
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        Optional<Map<String, IRecipeComponent>> components = Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC)
                .optionalFieldOf("components", Collections.emptyMap())
                .codec()
                .parse(ops, input)
                .resultOrPartial(errorBuilder::append);

        components.ifPresent(compNode -> {
            CompactCrafting.RECIPE_LOGGER.trace("Got components map; checking any exist and applying to recipe.");
            recipe.applyComponents(compNode);
        });

        if (debugOutput)
            CompactCrafting.RECIPE_LOGGER.debug("Finishing recipe decode.");

        return DataResult.success(Pair.of(recipe, input), Lifecycle.stable());
    }

    @Override
    public <T> DataResult<T> encode(MiniaturizationRecipe recipe, DynamicOps<T> ops, T prefix) {

        if (recipe == null) {
            return DataResult.error("Cannot serialize a null recipe.");
        }

        DataResult<T> layers = LAYER_CODEC.listOf().encodeStart(ops, recipe.getLayerListForCodecWrite());

        DataResult<T> components = Codec.unboundedMap(Codec.STRING, COMPONENT_CODEC)
                .encodeStart(ops, recipe.getComponents().getAllComponents());

        ItemStack catalystItem = recipe.getCatalyst();
        DataResult<T> catalyst = ItemStack.CODEC.encodeStart(ops, catalystItem == null ? ItemStack.EMPTY : catalystItem);

        DataResult<T> outputs = ItemStack.CODEC.listOf()
                .encodeStart(ops, ImmutableList.copyOf(recipe.getOutputs()));

        RecordBuilder<T> builder = ops.mapBuilder();

        builder.add("type", Codec.STRING.encodeStart(ops, "compactcrafting:miniaturization"));

        if (recipe.hasSpecifiedSize())
            builder.add("recipeSize", Codec.INT.encodeStart(ops, recipe.getRecipeSize()));

        return builder.add("layers", layers)
                .add("components", components)
                .add("catalyst", catalyst)
                .add("outputs", outputs)
                .build(prefix);
    }
}
