package com.robotgryphon.compactcrafting.recipes.layers;

import com.robotgryphon.compactcrafting.api.layers.IRecipeLayerBlocks;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeLayerBlocks implements IRecipeLayerBlocks, Cloneable {

    private final AxisAlignedBB layerBounds;
    private Map<String, Integer> componentTotals;
    private Map<BlockPos, BlockState> states;
    private Map<BlockPos, String> componentKeys;
    private Set<BlockPos> unmatched;

    private RecipeLayerBlocks(AxisAlignedBB bounds) {
        layerBounds = bounds;
        states = new HashMap<>();
        componentTotals = new HashMap<>();
        componentKeys = new HashMap<>();
        unmatched = new HashSet<>();
    }

    public static RecipeLayerBlocks create(IWorldReader world, MiniaturizationRecipe recipe, AxisAlignedBB bounds) {
        RecipeLayerBlocks instance = new RecipeLayerBlocks(bounds);

        BlockPos.betweenClosedStream(bounds).forEach(pos -> {
            BlockState state = world.getBlockState(pos);

            BlockPos normalizedPos = BlockSpaceUtil.normalizeLayerPosition(bounds, pos);

            instance.states.put(normalizedPos, state);

            // Pre-populate a set of component keys from the recipe instance, so we don't have to do it later
            Optional<String> compKey = recipe.getRecipeComponentKey(state);
            if (compKey.isPresent())
                instance.componentKeys.put(normalizedPos, compKey.get());
            else
                instance.unmatched.add(normalizedPos);

        });

        instance.rebuildComponentTotals();
        return instance;
    }

    private void rebuildComponentTotals() {
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
    public IRecipeLayerBlocks rotate(Rotation rotation) {
        if (rotation == Rotation.NONE) {
            try {
                return (IRecipeLayerBlocks) this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        BlockPos[] originalPositions = states.keySet().toArray(new BlockPos[0]);
        Map<BlockPos, BlockPos> layerRotated = BlockSpaceUtil.rotatePositionsInPlace(originalPositions, rotation);

        RecipeLayerBlocks rotBlocks = new RecipeLayerBlocks(this.layerBounds);
        for (BlockPos originalPos : layerRotated.keySet()) {
            BlockPos rotatedPos = layerRotated.get(originalPos);

            // Add original block state from world info
            BlockState state = this.states.get(originalPos);
            rotBlocks.states.put(rotatedPos, state);

            // If the state was previously unmatched, copy that over
            if (this.unmatched.contains(originalPos)) {
                rotBlocks.unmatched.add(rotatedPos);
                continue;
            }

            // Copy over the matched component key to the new position
            String key = this.componentKeys.get(originalPos);
            rotBlocks.componentKeys.put(rotatedPos, key);
        }

        rotBlocks.rebuildComponentTotals();
        return rotBlocks;
    }

    @Override
    public BlockState getRelativeState(BlockPos relativePos) {
        return states.get(relativePos);
    }

    @Override
    public int getNumberFilled() {
        return this.componentKeys.keySet().size();
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        RecipeLayerBlocks clone = new RecipeLayerBlocks(this.layerBounds);
        clone.unmatched = new HashSet<>(this.unmatched);
        clone.componentKeys = new HashMap<>(this.componentKeys);
        clone.states = new HashMap<>(this.states);

        return clone;
    }
}
