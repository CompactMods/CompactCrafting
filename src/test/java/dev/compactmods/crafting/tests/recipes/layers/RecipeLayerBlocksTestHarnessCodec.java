package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Map;
import java.util.zip.DataFormatException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeCodec;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import net.minecraft.util.math.BlockPos;

public class RecipeLayerBlocksTestHarnessCodec implements Codec<RecipeLayerBlocksTestHarness> {
    @Override
    public <T> DataResult<Pair<RecipeLayerBlocksTestHarness, T>> decode(DynamicOps<T> ops, T input) {
        RecipeLayerBlocksTestHarness harness = new RecipeLayerBlocksTestHarness();

        try {
            loadWorldData(ops, input, harness);
        } catch (DataFormatException e) {
            return DataResult.error(e.getMessage());
        }

        loadComponents(ops, input, harness);

        // Force unknown positions
        BlockPos.CODEC.listOf()
                .optionalFieldOf("forcedUnknownPositions")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error)
                .ifPresent(list -> {
                    list.forEach(harness::addForcedUnknownPosition);
                });

        return DataResult.success(Pair.of(harness, input));
    }

    private <T> void loadWorldData(DynamicOps<T> ops, T input, RecipeLayerBlocksTestHarness harness) throws DataFormatException {
        final IRecipeLayer world = MiniaturizationRecipeCodec.LAYER_CODEC
                .fieldOf("world")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        if (!(world instanceof IFixedSizedRecipeLayer)) {
            throw new DataFormatException("Test harness only works with fixed-sized world layers. TBC in the future.");
        }

        // Currently, it's just Mixed layer types - those calculate their dimensions during construction
        harness.bounds = ((IFixedSizedRecipeLayer) world).getDimensions();
        harness.worldLayerDef = world;

        final Map<String, BlockComponent> worldBlocks = Codec.unboundedMap(Codec.STRING, BlockComponent.CODEC)
                .fieldOf("worldBlocks")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        for (Map.Entry<String, BlockComponent> e : worldBlocks.entrySet()) {
            e.getValue().getFirstMatch().ifPresent(bs -> harness.states.put(e.getKey(), bs));
        }
    }

    private <T> void loadComponents(DynamicOps<T> ops, T input, RecipeLayerBlocksTestHarness harness) {
        final Map<String, IRecipeComponent> components = Codec.unboundedMap(Codec.STRING, MiniaturizationRecipeCodec.COMPONENT_CODEC)
                .fieldOf("components")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        CCMiniRecipeComponents test = new CCMiniRecipeComponents();
        components.forEach((ck, c) -> {
            if (c instanceof IRecipeBlockComponent)
                test.registerBlock(ck, (IRecipeBlockComponent) c);
            else
                test.registerOther(ck, c);
        });

        harness.rebuildComponentTotals();
    }

    @Override
    public <T> DataResult<T> encode(RecipeLayerBlocksTestHarness input, DynamicOps<T> ops, T prefix) {
        return DataResult.error("Not supported");
    }
}
