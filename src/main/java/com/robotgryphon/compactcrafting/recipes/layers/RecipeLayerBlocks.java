package com.robotgryphon.compactcrafting.recipes.layers;

import dev.compactmods.compactcrafting.api.recipe.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeLayerBlocks implements IRecipeLayerBlocks, Cloneable {

    private final AxisAlignedBB layerBounds;
    private Map<String, Integer> componentTotals;
    private final Map<BlockPos, BlockState> states;
    private final Map<BlockPos, String> componentKeys;
    private final Set<BlockPos> unmatchedStates;

    protected RecipeLayerBlocks(AxisAlignedBB bounds) {
        layerBounds = bounds;
        states = new HashMap<>();
        componentTotals = new HashMap<>();
        componentKeys = new HashMap<>();
        unmatchedStates = new HashSet<>();
    }

    public RecipeLayerBlocks(IRecipeLayerBlocks original) {
        this(original.getBounds());

        // Loops all block positions inside bounds, for-each
        BlockPos.betweenClosedStream(layerBounds).forEach(pos -> {
            Optional<String> key = original.getComponentAtPosition(pos);
            if (key.isPresent()) componentKeys.put(pos, key.get());
            else unmatchedStates.add(pos);

            original.getStateAtPosition(pos).ifPresent(state -> states.put(pos, state));
        });
    }

    RecipeLayerBlocks(AxisAlignedBB bounds, Map<BlockPos, BlockState> states,
        Map<BlockPos, String> components, Set<BlockPos> unmatchedStates) {
        this.layerBounds = bounds;
        this.states = states;
        this.componentKeys = components;
        this.unmatchedStates = unmatchedStates;

        this.rebuildComponentTotals();
    }

    public static RecipeLayerBlocks create(IWorldReader world, MiniaturizationRecipe recipe, AxisAlignedBB bounds) {
        RecipeLayerBlocks instance = new RecipeLayerBlocks(bounds);

        BlockPos.betweenClosedStream(bounds).forEach(pos -> {
            if(!bounds.contains(pos.getX(), pos.getY(), pos.getZ()))
                return;

            BlockState state = world.getBlockState(pos);

            BlockPos normalizedPos = BlockSpaceUtil.normalizeLayerPosition(bounds, pos);

            instance.states.put(normalizedPos, state);

            // Pre-populate a set of component keys from the recipe instance, so we don't have to do it later
            Optional<String> compKey = recipe.getRecipeComponentKey(state);
            if (compKey.isPresent())
                instance.componentKeys.put(normalizedPos, compKey.get());
            else
                instance.unmatchedStates.add(normalizedPos);

        });

        instance.rebuildComponentTotals();
        return instance;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.layerBounds;
    }

    void rebuildComponentTotals() {
        this.componentTotals = componentKeys.entrySet()
            .stream()
            .collect(
                Collectors.groupingBy(
                    // Group by map value (aka component key)
                    Map.Entry::getValue,

                    // Map keys (the blockpos entries) are summed up (like list::size)
                    Collectors.mapping(
                        Map.Entry::getKey,
                        Collectors.reducing(0, e -> 1, Integer::sum)
                    )
                )
            );
    }

    @Override
    public Optional<String> getComponentAtPosition(BlockPos relative) {
        return Optional.ofNullable(componentKeys.get(relative));
    }

    @Override
    public Optional<BlockState> getStateAtPosition(BlockPos relative) {
        return Optional.ofNullable(states.get(relative));
    }

    @Override
    public Stream<BlockPos> getPositions() {
        return states.keySet().stream();
    }

    /**
     * Gets the number of unique component keys in this set of blocks.
     *
     * @return
     */
    @Override
    public int getNumberUniqueComponents() {
        return this.getComponentTotals().keySet().size();
    }

    @Override
    public Map<String, Integer> getComponentTotals() {
        return componentTotals;
    }
}
