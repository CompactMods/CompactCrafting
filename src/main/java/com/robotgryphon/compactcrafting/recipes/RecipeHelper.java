package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Stream;

public abstract class RecipeHelper {

    public static BlockState getBlockStateForNormalizedLocation(IWorldReader world, BlockPos normalizedPos, AxisAlignedBB layerBounds) {
        Vector3i offset = new Vector3i(layerBounds.minX, layerBounds.minY, layerBounds.minZ);
        BlockPos offsetPos = normalizedPos.add(offset);

        return world.getBlockState(offsetPos);
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
                .map(p -> new BlockPos(
                        p.getX() - layerBounds.minX,
                        p.getY() - layerBounds.minY,
                        p.getZ() - layerBounds.minZ
                ))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }

    public static AxisAlignedBB getBoundsForBlocks(Collection<BlockPos> filled) {
        if(filled.size() == 0)
            return AxisAlignedBB.withSizeAtOrigin(0, 0, 0);

        MutableBoundingBox trimmedBounds = null;
        for(BlockPos filledPos : filled) {
            if(trimmedBounds == null) {
                trimmedBounds = new MutableBoundingBox(filledPos, filledPos);
                continue;
            }

            MutableBoundingBox checkPos = new MutableBoundingBox(filledPos, filledPos);
            if(!trimmedBounds.intersectsWith(checkPos))
                trimmedBounds.expandTo(checkPos);
        }

        return AxisAlignedBB.toImmutable(trimmedBounds);
    }

    public static String[][] getTrimmedTemplateForLayer(IRecipeLayer layer, AxisAlignedBB fieldBounds) {
        Set<BlockPos> nonAir = layer.getNonAirPositions();
        AxisAlignedBB bounds = getBoundsForBlocks(nonAir);

        int dim = (int) fieldBounds.getXSize();

        String[][] components = new String[dim][dim];

        for(BlockPos filledPos : nonAir) {
            String layerRequirement = layer.getRequiredComponentKeyForPosition(filledPos);

            if(layerRequirement != null)
                components[filledPos.getX()][filledPos.getZ()] = layerRequirement;
        }

        return components;
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
        AxisAlignedBB trimmedBounds = getBoundsForBlocks(Arrays.asList(filledLocations));
        BlockPos[] normalizedLocations = normalizeLayerPositions(trimmedBounds, filledLocations);

        String[][] template = getTrimmedTemplateForLayer(layer, fieldBounds);

        // Finally, simply check the normalized template
        for(BlockPos pos : normalizedLocations) {
            String key = layer.getRequiredComponentKeyForPosition(pos);
            if(key == null)
                return false;

            BlockState state = getBlockStateForNormalizedLocation(world, pos, layer.getDimensions());
            Optional<String> worldKey = recipe.getRecipeComponentKey(state);

            // Is the position the correct block?
            boolean keyCorrect = worldKey.filter(key::equals).isPresent();
            if(!keyCorrect)
                return false;
        }

        return true;
    }
}
