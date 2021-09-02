package com.robotgryphon.compactcrafting.tests.recipes.layers;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.recipes.layers.MixedComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerComponentPositionLookup;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayerBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RecipeLayerBlocksTestHarness implements IRecipeLayerBlocks {

    private RecipeLayerComponentPositionLookup lookup;
    private AxisAlignedBB bounds;
    private final Map<String, BlockState> knownComponents;
    private Map<String, Integer> knownComponentTotals;

    // TODO - Build up instances and actually test this with the unit testing libs

    private RecipeLayerBlocksTestHarness() {
        this.lookup = null;
        this.bounds = AxisAlignedBB.ofSize(0, 0, 0);
        this.knownComponents = new HashMap<>();
    }

    /**
     * Closely follows the specs for defining a mixed layer type; all components are loaded in
     * using the standard codecs that the recipe system uses internally.
     *
     * @param json
     * @return
     */
    @Nullable
    public static RecipeLayerBlocksTestHarness fromJson(JsonObject json) {
        if(!json.has("components") || !json.has("layer"))
            return null;

        final MixedComponentRecipeLayer layer = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject("layer"))
                .getOrThrow(false, CompactCrafting.RECIPE_LOGGER::error);

        RecipeLayerBlocksTestHarness harness = new RecipeLayerBlocksTestHarness();
        harness.lookup = layer.getComponentLookup();
        harness.bounds = layer.getDimensions();

        harness.rebuildComponentTotals();
        return harness;
    }

    @Override
    public Optional<String> getComponentAtPosition(BlockPos relative) {
        return lookup.getRequiredComponentKeyForPosition(relative);
    }

    @Override
    public Optional<BlockState> getStateAtPosition(BlockPos relative) {
        String worldKey = lookup.getRequiredComponentKeyForPosition(relative).orElse("?");
        if(knownComponents.containsKey(worldKey))
            return Optional.ofNullable(knownComponents.get(worldKey));

        return Optional.empty();
    }

    @Override
    public Stream<BlockPos> getPositions() {
        return BlockSpaceUtil.getLayerBlockPositions(this.bounds);
    }

    void rebuildComponentTotals() {
        final Map<String, Integer> worldTotals = new HashMap<>();
        lookup.getComponentTotals()
                .entrySet()
                .stream()
                .filter(es -> knownComponents.containsKey(es.getKey()))
                .forEach(es -> worldTotals.put(es.getKey(), es.getValue()));

        this.knownComponentTotals = worldTotals;
    }

    /**
     * Gets the number of unique component keys in this set of blocks.
     *
     * @return
     */
    @Override
    public int getNumberKnownComponents() {
        return knownComponents.size();
    }

    @Override
    public Map<String, Integer> getKnownComponentTotals() {
        return this.knownComponentTotals;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    @Override
    public boolean allIdentified() {
        return lookup.getComponents()
                .stream()
                .anyMatch(knownKey -> !knownComponents.containsKey(knownKey));
    }
}
