package dev.compactmods.crafting.util;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;

public abstract class BlockSpaceUtil {

    public static AxisAlignedBB getLayerBounds(MiniaturizationFieldSize fieldSize, int layerOffset) {
        AxisAlignedBB fieldBounds = fieldSize.getBoundsAtOrigin();
        return getLayerBounds(fieldBounds, layerOffset);
    }

    public static AxisAlignedBB getLayerBounds(AxisAlignedBB fieldBounds, int layerOffset) {
        return new AxisAlignedBB(
                new Vector3d(fieldBounds.minX, fieldBounds.minY + layerOffset, fieldBounds.minZ),
                new Vector3d(fieldBounds.maxX, (fieldBounds.minY + layerOffset) + 1, fieldBounds.maxZ)
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

    public static boolean boundsFitsInside(AxisAlignedBB inner, AxisAlignedBB outer) {
        if(inner.getZsize() > outer.getZsize())
            return false;

        if(inner.getXsize() > outer.getXsize())
            return false;

        if(inner.getYsize() > outer.getYsize())
            return false;

        return true;
    }

    public static Stream<BlockPos> getBlocksIn(MiniaturizationFieldSize fieldSize, int layerOffset) {
        AxisAlignedBB layerBounds = getLayerBounds(fieldSize, layerOffset);
        return getBlocksIn(layerBounds);
    }

    @Nonnull
    public static Stream<BlockPos> getBlocksIn(AxisAlignedBB bounds) {
        return BlockPos.betweenClosedStream(bounds.contract(1, 1, 1));
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

    public static AxisAlignedBB getBoundsForBlocks(Stream<BlockPos> positions) {
        final Set<BlockPos> collect = positions.map(BlockPos::immutable).collect(Collectors.toSet());
        return getBoundsForBlocks(collect);
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

    public static Stream<BlockPos> getWallPositions(AxisAlignedBB bounds) {
        AxisAlignedBB layerBounds = new AxisAlignedBB(0, 0, 0, bounds.getXsize() - 1, 0, bounds.getZsize() - 1);
        AxisAlignedBB insideBounds = layerBounds.move(1, 0, 1).contract(2, 0, 2);

        Set<BlockPos> positions = BlockSpaceUtil.getBlocksIn(layerBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Set<BlockPos> inside = BlockSpaceUtil.getBlocksIn(insideBounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        positions.removeAll(inside);
        return positions.stream();
    }
}
