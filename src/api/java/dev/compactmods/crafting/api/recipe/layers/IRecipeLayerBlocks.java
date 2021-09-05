package dev.compactmods.crafting.api.recipe.layers;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface IRecipeLayerBlocks {

    Optional<String> getComponentAtPosition(BlockPos relative);

    /**
     * Returns the block state at the relative position in the world.
     * If the state could not be mapped (ie in the case of an unknown component
     * at the recipe level) then this should return an empty optional.
     *
     * @param relative The relative location of the blockspace, in the layer.
     * @return A filled Optional if the state is mapped, empty otherwise.
     */
    Optional<BlockState> getStateAtPosition(BlockPos relative);

    Stream<BlockPos> getPositions();

    /**
     * Gets the number of unique component keys in this set of blocks.
     * @return
     */
    int getNumberKnownComponents();

    Map<String, Integer> getKnownComponentTotals();

    AxisAlignedBB getBounds();

    boolean allIdentified();
}
