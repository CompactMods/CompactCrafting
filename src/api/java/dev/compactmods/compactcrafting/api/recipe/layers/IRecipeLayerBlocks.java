package dev.compactmods.compactcrafting.api.recipe.layers;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface IRecipeLayerBlocks {

    Optional<String> getComponentAtPosition(BlockPos relative);

    Optional<BlockState> getStateAtPosition(BlockPos relative);

    Stream<BlockPos> getPositions();

    /**
     * Gets the number of unique component keys in this set of blocks.
     * @return
     */
    int getNumberUniqueComponents();

    Map<String, Integer> getComponentTotals();

    AxisAlignedBB getBounds();
}
