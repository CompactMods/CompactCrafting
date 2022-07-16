package dev.compactmods.crafting.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.catalyst.CatalystMatcherCodec;
import dev.compactmods.crafting.recipes.catalyst.ItemStackCatalystMatcher;
import dev.compactmods.crafting.recipes.components.RecipeComponentTypeCodec;
import dev.compactmods.crafting.recipes.layers.RecipeLayerTypeCodec;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.world.item.ItemStack;

public class MiniaturizationRecipeCodec implements Codec<MiniaturizationRecipe> {

    public static final Codec<IRecipeLayer> LAYER_CODEC =
            RecipeLayerTypeCodec.INSTANCE.dispatchStable(IRecipeLayer::getType, RecipeLayerType::getCodec);

    public static final Codec<IRecipeComponent> COMPONENT_CODEC =
            RecipeComponentTypeCodec.INSTANCE.dispatchStable(IRecipeComponent::getType, RecipeComponentType::getCodec);

    MiniaturizationRecipeCodec() {
    }

    @Override
    public <T> DataResult<Pair<MiniaturizationRecipe, T>> decode(DynamicOps<T> ops, T input) {
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

        // if we don't have a fixed size layer to base dimensions off of, and the recipe size won't fit in a field
        if (!hasFixedLayers && !MiniaturizationFieldSize.canFitDimensions(recipeSize)) {
            errorBuilder.append("Specified recipe size will not fit in a crafting field: ").append(recipeSize);
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        recipe.recalculateDimensions();

        final Optional<T> catalystNode = ops.get(input, "catalyst").result();
        if (catalystNode.isEmpty()) {
            CompactCrafting.LOGGER.warn("No catalyst node defined in recipe; this is likely a bad file!");
        } else {
            final Optional<T> catalystType = ops.get(catalystNode.get(), "type").result();
            if (catalystType.isEmpty()) {
                CompactCrafting.LOGGER.warn("Error: no catalyst type defined; falling back to the itemstack handler.");

                final ItemStack stackData = ItemStack.CODEC
                        .fieldOf("catalyst").codec()
                        .parse(ops, input)
                        .resultOrPartial(errorBuilder::append)
                        .orElse(ItemStack.EMPTY);

                recipe.setCatalyst(new ItemStackCatalystMatcher(stackData));
            } else {
                ICatalystMatcher catalyst = CatalystMatcherCodec.MATCHER_CODEC
                        .fieldOf("catalyst").codec()
                        .parse(ops, input)
                        .resultOrPartial(errorBuilder::append)
                        .orElse(new ItemStackCatalystMatcher(ItemStack.EMPTY));

                // ICatalystMatcher catalyst = new ItemTagCatalystMatcher(ItemTags.PLANKS);
                recipe.setCatalyst(catalyst);
            }
        }

        Optional<List<ItemStack>> outputs = ItemStack.CODEC.listOf()
                .fieldOf("outputs")
                .codec()
                .parse(ops, input)
                .resultOrPartial(errorBuilder::append);

        outputs.ifPresent(recipe::setOutputs);
        if (!outputs.isPresent()) {
            return DataResult.error(errorBuilder.toString(), Pair.of(recipe, input), Lifecycle.stable());
        }

        if (recipe.getOutputs().length == 0) {
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

        ICatalystMatcher catalystItem = recipe.getCatalyst();

        DataResult<T> catalyst;
        if (catalystItem != null) {
            catalyst = CatalystMatcherCodec.MATCHER_CODEC.encodeStart(ops, catalystItem);
        } else {
            CompactCrafting.RECIPE_LOGGER.warn("Catalyst appears to be missing.");
            catalyst = DataResult.success(null);
        }

        DataResult<T> outputs = ItemStack.CODEC.listOf()
                .encodeStart(ops, ImmutableList.copyOf(recipe.getOutputs()));

        RecordBuilder<T> builder = ops.mapBuilder();

        builder.add("type", Codec.STRING.encodeStart(ops, "compactcrafting:miniaturization"));

        if (recipe.hasSpecifiedSize())
            builder.add("recipeSize", Codec.INT.encodeStart(ops, recipe.getRecipeSize()));

        var b = builder.add("layers", layers)
                .add("components", components);

        if (catalystItem != null)
            b.add("catalyst", catalyst);

        return b.add("outputs", outputs)
                .build(prefix);
    }
}
