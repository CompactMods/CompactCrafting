package com.robotgryphon.compactcrafting.recipes;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Stream;

public abstract class RecipeHelper {

    /**
     * Normalizes world coordinates to relative field coordinates.
     *
     * @param fieldBounds The bounds of the field itself.
     * @param pos The position to normalize.
     * @return
     */
    public static BlockPos normalizeLayerPosition(AxisAlignedBB fieldBounds, BlockPos pos) {
        return new BlockPos(
                pos.getX() - fieldBounds.minX,
                pos.getY() - fieldBounds.minY,
                pos.getZ() - fieldBounds.minZ
        );
    }
    /**
     * Converts world-coordinate positions into relative field positions.
     *
     * @param fieldBounds The boundaries of the crafting field.
     * @param fieldPositions The non-air block positions in the field (world coordinates).
     * @return
     */
    public static BlockPos[] normalizeLayerPositions(AxisAlignedBB fieldBounds, BlockPos[] fieldPositions) {
        // Normalize the block positions so the recipe can match easier
        return Stream.of(fieldPositions)
                .parallel()
                .map(p -> normalizeLayerPosition(fieldBounds, p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }

    public static BlockPos denormalizeLayerPosition(AxisAlignedBB realBounds, BlockPos normPos) {
        return new BlockPos(
                realBounds.minX + normPos.getX(),
                realBounds.minY + normPos.getY(),
                realBounds.minZ + normPos.getZ()
        );
    }
}
