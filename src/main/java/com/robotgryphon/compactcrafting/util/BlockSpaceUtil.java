package com.robotgryphon.compactcrafting.util;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class BlockSpaceUtil {

    public static BlockPos[] getFilledBlocksByLayer(IWorldReader world, AxisAlignedBB fieldFilledBounds, int layer) {
        // Outside of field bounds, ignore
        if(layer < 0 || layer > fieldFilledBounds.maxY)
            return new BlockPos[0];

        AxisAlignedBB layerFilledBounds = getLayerBoundsByYOffset(fieldFilledBounds, layer);
       return BlockPos.getAllInBox(layerFilledBounds)
                .filter(pos -> pos.getY() == layerFilledBounds.minY)
                .filter(pos -> !world.isAirBlock(pos))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }

    public static AxisAlignedBB getLayerBoundsByYOffset(AxisAlignedBB fullBounds, int yOffset) {
        return new AxisAlignedBB(
                new Vector3d(fullBounds.minX, fullBounds.minY + yOffset, fullBounds.minZ),
                new Vector3d(fullBounds.maxX, (fullBounds.minY + yOffset) + 1, fullBounds.maxZ)
        );
    }

    public static BlockPos[] rotatePositionsInPlace(BlockPos[] positions) {
        return rotatePositionsInPlace(positions, Rotation.CLOCKWISE_90);
    }

    public static BlockPos rotatePositionInPlace(AxisAlignedBB bounds, BlockPos rotated, Rotation rotation) {
        Rotation rotBack = rotation.add(Rotation.CLOCKWISE_180);

        BlockPos normalized = normalizeLayerPosition(bounds, rotated);
        BlockPos rotatedBack = normalized.rotate(rotBack);
        BlockPos denormalized = denormalizeLayerPosition(bounds, rotatedBack);

        AxisAlignedBB boundsRotated = new AxisAlignedBB(denormalized, denormalized);
        BlockPos reNormalized = normalizeLayerPosition(boundsRotated, denormalized);

        return denormalizeLayerPosition(bounds, reNormalized);

    }

    public static BlockPos[] rotatePositionsInPlace(BlockPos[] positions, Rotation rot) {
        AxisAlignedBB bounds = getBoundsForBlocks(positions);

        // Rotation around a normalized world offset (smallest position is 0,0)
        BlockPos[] rotatedPreNormalize = Stream.of(positions)
                .map(p -> normalizeLayerPosition(bounds, p))
                .map(p -> p.rotate(rot))
                .map(p -> denormalizeLayerPosition(bounds, p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        AxisAlignedBB rotatedBounds = BlockSpaceUtil.getBoundsForBlocks(rotatedPreNormalize);

        // Re-Normalize the positions to fix the offsetting that rotation does (we're rotating in place)
        BlockPos[] reNormalized = normalizeLayerPositions(rotatedBounds, rotatedPreNormalize);

        // Rotated positions were normalized before to fix the offsetting that rotation does - denormalize again
        return Stream.of(reNormalized)
                .map(p -> denormalizeLayerPosition(bounds, p))
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);
    }

    public static boolean boundsFitsInside(AxisAlignedBB check, AxisAlignedBB space) {
        if(check.getZSize() > space.getZSize())
            return false;

        if(check.getXSize() > space.getXSize())
            return false;

        if(check.getYSize() > space.getYSize())
            return false;

        return true;
    }

    public static AxisAlignedBB getBoundsForBlocks(BlockPos[] filled) {
        return getBoundsForBlocks(Arrays.asList(filled));
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
