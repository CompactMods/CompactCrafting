package dev.compactmods.crafting.api.recipe.layers;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public interface IRecipeBlocks {

    Optional<String> getComponentAtPosition(BlockPos relative);

    /**
     * Returns the block state at the relative position in the world.
     * If the state could not be mapped (ie in the case of an unknown component
     * at the recipe level) then this should return an empty optional.
     *
     * @param relative The relative location of the blockspace, in the layer.
     * @return A filled Optional if the state is mapped, empty otherwise.
     */
    BlockState getStateAtPosition(BlockPos relative);

    Stream<BlockPos> getPositions();

    /**
     * Gets the number of unique component keys in this set of blocks.
     * @return
     */
    int getNumberKnownComponents();

    void rebuildComponentTotals();
    
    Map<String, Integer> getKnownComponentTotals();

    AxisAlignedBB getSourceBounds();

    boolean allIdentified();

    Stream<BlockPos> getUnmappedPositions();

    Stream<BlockPos> getPositionsForComponent(String component);

    AxisAlignedBB getFilledBounds();

    IRecipeBlocks slice(AxisAlignedBB bounds);

    IRecipeBlocks offset(Vector3i amount);

    default IRecipeBlocks below(int offset) {
        return offset(new Vector3i(0, -offset, 0));
    }

    default IRecipeBlocks above(int offset) {
        return offset(new Vector3i(0, offset, 0));
    }

    default IRecipeBlocks normalize() {
        AxisAlignedBB sb = getSourceBounds();
        BlockPos offset = new BlockPos(-sb.minX, -sb.minY, -sb.minZ);
        return offset(offset);
    }
}
