package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Map;
import java.util.zip.DataFormatException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.dim.IFixedSizedRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeCodec;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import net.minecraft.util.math.BlockPos;

public class TestRecipeLayerBlocksCodec implements Codec<TestRecipeLayerBlocks> {
    @Override
    public <T> DataResult<Pair<TestRecipeLayerBlocks, T>> decode(DynamicOps<T> ops, T input) {
        TestRecipeLayerBlocks harness = new TestRecipeLayerBlocks();

        try {
            loadWorldData(ops, input, harness);
        } catch (DataFormatException e) {
            return DataResult.error(e.getMessage());
        }

        // Force unknown positions
        BlockPos.CODEC.listOf()
                .optionalFieldOf("forcedUnknownPositions")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error)
                .ifPresent(list -> {
                    list.forEach(harness::addForcedUnknownPosition);
                });

        harness.rebuildComponentTotals();

        return DataResult.success(Pair.of(harness, input));
    }

    private <T> void loadWorldData(DynamicOps<T> ops, T input, TestRecipeLayerBlocks harness) throws DataFormatException {
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

        final Map<String, BlockComponent> blocks = Codec.unboundedMap(Codec.STRING, BlockComponent.CODEC)
                .fieldOf("blocks")
                .codec()
                .parse(ops, input)
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        for (Map.Entry<String, BlockComponent> e : blocks.entrySet()) {
            e.getValue().getFirstMatch().ifPresent(bs -> harness.states.put(e.getKey(), bs));
        }
    }

    @Override
    public <T> DataResult<T> encode(TestRecipeLayerBlocks input, DynamicOps<T> ops, T prefix) {
        return DataResult.error("Not supported");
    }
}
