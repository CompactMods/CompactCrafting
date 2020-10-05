package com.robotgryphon.compactcrafting.recipes;

import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class RecipeHelper {

    public static BlockState getBlockStateForNormalizedLocation(IWorldReader world, BlockPos normalizedPos, AxisAlignedBB layerBounds) {
        Vector3i offset = new Vector3i(layerBounds.minX, layerBounds.minY, layerBounds.minZ);
        BlockPos offsetPos = normalizedPos.add(offset);

        return world.getBlockState(offsetPos);
    }

    public static BlockPos normalizeLayerPosition(AxisAlignedBB layerBounds, BlockPos pos) {
        return new BlockPos(
                pos.getX() - layerBounds.minX,
                pos.getY() - layerBounds.minY,
                pos.getZ() - layerBounds.minZ
        );
    }
    /**
     * Converts world-coordinate positions into relative field positions.
     *
     * @param layerBounds The boundaries of the crafting field layer.
     * @param fieldPositions The non-air block positions in the field (world coordinates).
     * @return
     */
    public static BlockPos[] normalizeLayerPositions(AxisAlignedBB layerBounds, BlockPos[] fieldPositions) {
        // Normalize the block positions so the recipe can match easier
        return Stream.of(fieldPositions)
                .parallel()
                .map(p -> normalizeLayerPosition(layerBounds, p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }

    /**
     * Checks if a layer matches a template by extracting information about components
     * from the various filled positions.
     *
     * @param world
     * @param layer
     * @param filledLocations A world-space mapping of non-air blocks.
     * @return
     */
    public static boolean layerMatchesTemplate(IWorldReader world, MiniaturizationRecipe recipe, IRecipeLayer layer, AxisAlignedBB fieldBounds, BlockPos[] filledLocations) {
        AxisAlignedBB trimmedBounds = BlockSpaceUtil.getBoundsForBlocks(Arrays.asList(filledLocations));
        BlockPos[] normalizedLocations = normalizeLayerPositions(trimmedBounds, filledLocations);

        // Finally, simply check the normalized template
        for(BlockPos pos : normalizedLocations) {
            String key = layer.getRequiredComponentKeyForPosition(pos);
            if(key == null)
                return false;

            BlockState state = getBlockStateForNormalizedLocation(world, pos, fieldBounds);
            Optional<String> worldKey = recipe.getRecipeComponentKey(state);

            // Is the position the correct block?
            boolean keyCorrect = worldKey.filter(key::equals).isPresent();
            if(!keyCorrect)
                return false;
        }

        return true;
    }
}
