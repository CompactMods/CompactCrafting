package dev.compactmods.crafting.api.recipe.layers;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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

    AABB getSourceBounds();

    boolean allIdentified();

    Stream<BlockPos> getUnmappedPositions();

    Stream<BlockPos> getPositionsForComponent(String component);

    AABB getFilledBounds();

    IRecipeBlocks slice(AABB bounds);

    IRecipeBlocks offset(Vec3i amount);

    default IRecipeBlocks below(int offset) {
        return offset(new Vec3i(0, -offset, 0));
    }

    default IRecipeBlocks above(int offset) {
        return offset(new Vec3i(0, offset, 0));
    }

    default IRecipeBlocks normalize() {
        AABB sb = getSourceBounds();
        BlockPos offset = new BlockPos(-sb.minX, -sb.minY, -sb.minZ);
        return offset(offset);
    }
}
