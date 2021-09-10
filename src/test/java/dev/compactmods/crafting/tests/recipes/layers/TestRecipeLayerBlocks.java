package dev.compactmods.crafting.tests.recipes.layers;

import java.util.*;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TestRecipeLayerBlocks implements IRecipeLayerBlocks {

    AxisAlignedBB bounds;
    final Map<String, BlockState> states;
    Map<String, Integer> knownComponentTotals;
    IRecipeLayer worldLayerDef;
    final Set<BlockPos> unmatchedStates;

    public static final Codec<TestRecipeLayerBlocks> CODEC = new TestRecipeLayerBlocksCodec();

    TestRecipeLayerBlocks() {
        this.bounds = AxisAlignedBB.ofSize(0, 0, 0);
        this.states = new HashMap<>();
        this.unmatchedStates = new HashSet<>();
    }

    @Override
    public Optional<String> getComponentAtPosition(BlockPos relative) {
        return worldLayerDef.getComponentForPosition(relative);
    }

    @Override
    public Optional<BlockState> getStateAtPosition(BlockPos relative) {
        if(this.unmatchedStates.contains(relative))
            return Optional.empty();

        return getComponentAtPosition(relative).map(states::get);
    }

    @Override
    public Stream<BlockPos> getPositions() {
        return BlockSpaceUtil.getBlocksIn(this.bounds);
    }

    void rebuildComponentTotals() {
        final Map<String, Integer> worldTotals = new HashMap<>();
        worldLayerDef.getComponentTotals()
                .entrySet()
                .stream()
                .filter(es -> states.containsKey(es.getKey()))
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
        return states.size();
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
        return worldLayerDef.getComponents()
                .stream()
                .allMatch(states::containsKey);
    }

    @Override
    public Stream<BlockPos> getPositionsForComponent(String component) {
        return worldLayerDef.getPositionsForComponent(component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRecipeLayerBlocks that = (TestRecipeLayerBlocks) o;
        return Objects.equals(bounds, that.bounds) && Objects.equals(states, that.states) && Objects.equals(knownComponentTotals, that.knownComponentTotals) && Objects.equals(worldLayerDef, that.worldLayerDef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bounds, states, knownComponentTotals, worldLayerDef);
    }

    public void addForcedUnknownPosition(BlockPos position) {
        this.unmatchedStates.add(position);
    }
}
