package com.robotgryphon.compactcrafting.util;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BlockSpaceUtil {

    public static AxisAlignedBB getLayerBoundsByYOffset(AxisAlignedBB fullBounds, int yOffset) {
        return new AxisAlignedBB(
                new Vector3d(fullBounds.minX, fullBounds.minY + yOffset, fullBounds.minZ),
                new Vector3d(fullBounds.maxX, (fullBounds.minY + yOffset) + 1, fullBounds.maxZ)
        );
    }

    public static Map<BlockPos, BlockPos> rotatePositionsInPlace(BlockPos[] positions) {
        return rotatePositionsInPlace(positions, Rotation.CLOCKWISE_90);
    }

    public static Map<BlockPos, BlockPos> rotatePositionsInPlace(BlockPos[] positions, Rotation rot) {
        AxisAlignedBB bounds = getBoundsForBlocks(positions);

        // Rotation around a normalized world offset (smallest position is 0,0)
        Map<BlockPos, BlockPos> rotatedPreNormalize = Stream.of(positions)
                .map(p -> new BlockPos[] { p, normalizeLayerPosition(bounds, p) })
                .map(p -> new BlockPos[] { p[0], p[1].rotate(rot) })
                .map(p -> new BlockPos[] { p[0], denormalizeLayerPosition(bounds, p[1]) })
                .map(p -> new BlockPos[] { p[0], p[1].immutable() })
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));

        AxisAlignedBB rotatedBounds = BlockSpaceUtil.getBoundsForBlocks(rotatedPreNormalize.values());

        // Re-Normalize the positions to fix the offsetting that rotation does (we're rotating in place)
        Map<BlockPos, BlockPos> realPositions = new HashMap<>();
        rotatedPreNormalize.forEach((k, rp) -> {
            BlockPos reNormalized = normalizeLayerPosition(rotatedBounds, rp);
            BlockPos realPosition = denormalizeLayerPosition(bounds, reNormalized);
            realPositions.put(k.immutable(), realPosition.immutable());
        });

        return realPositions;
    }

    public static boolean boundsFitsInside(AxisAlignedBB check, AxisAlignedBB space) {
        if(check.getZsize() > space.getZsize())
            return false;

        if(check.getXsize() > space.getXsize())
            return false;

        if(check.getYsize() > space.getYsize())
            return false;

        return true;
    }

    public static AxisAlignedBB getBoundsForBlocks(BlockPos[] filled) {
        return getBoundsForBlocks(Arrays.asList(filled));
    }

    public static AxisAlignedBB getBoundsForBlocks(Collection<BlockPos> filled) {
        if(filled.size() == 0)
            return AxisAlignedBB.ofSize(0, 0, 0);

        MutableBoundingBox trimmedBounds = null;
        for(BlockPos filledPos : filled) {
            if(trimmedBounds == null) {
                trimmedBounds = new MutableBoundingBox(filledPos, filledPos);
                continue;
            }

            MutableBoundingBox checkPos = new MutableBoundingBox(filledPos, filledPos);
            if(!trimmedBounds.intersects(checkPos))
                trimmedBounds.expand(checkPos);
        }

        return AxisAlignedBB.of(trimmedBounds);
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
                .map(BlockPos::immutable)
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
