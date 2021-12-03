package dev.compactmods.crafting.util;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public abstract class BlockSpaceUtil {

    public static AABB getLayerBounds(MiniaturizationFieldSize fieldSize, int layerOffset) {
        AABB fieldBounds = fieldSize.getBoundsAtOrigin();
        return getLayerBounds(fieldBounds, layerOffset);
    }

    public static AABB getLayerBounds(AABB fieldBounds, int layerOffset) {
        return new AABB(
                new Vec3(fieldBounds.minX, fieldBounds.minY + layerOffset, fieldBounds.minZ),
                new Vec3(fieldBounds.maxX, (fieldBounds.minY + layerOffset) + 1, fieldBounds.maxZ)
        );
    }

    public static Map<BlockPos, BlockPos> rotatePositionsInPlace(BlockPos[] positions) {
        return rotatePositionsInPlace(positions, Rotation.CLOCKWISE_90);
    }

    public static Map<BlockPos, BlockPos> rotatePositionsInPlace(BlockPos[] positions, Rotation rot) {
        AABB bounds = getBoundsForBlocks(positions);

        // Rotation around a normalized world offset (smallest position is 0,0)
        Map<BlockPos, BlockPos> rotatedPreNormalize = Stream.of(positions)
                .map(p -> new BlockPos[]{p, normalizeLayerPosition(bounds, p)})
                .map(p -> new BlockPos[]{p[0], p[1].rotate(rot)})
                .map(p -> new BlockPos[]{p[0], denormalizeLayerPosition(bounds, p[1])})
                .map(p -> new BlockPos[]{p[0], p[1].immutable()})
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));

        AABB rotatedBounds = BlockSpaceUtil.getBoundsForBlocks(rotatedPreNormalize.values());

        // Re-Normalize the positions to fix the offsetting that rotation does (we're rotating in place)
        Map<BlockPos, BlockPos> realPositions = new HashMap<>();
        rotatedPreNormalize.forEach((k, rp) -> {
            BlockPos reNormalized = normalizeLayerPosition(rotatedBounds, rp);
            BlockPos realPosition = denormalizeLayerPosition(bounds, reNormalized);
            realPositions.put(k.immutable(), realPosition.immutable());
        });

        return realPositions;
    }

    public static boolean boundsFitsInside(AABB inner, AABB outer) {
        if (inner.getZsize() > outer.getZsize())
            return false;

        if (inner.getXsize() > outer.getXsize())
            return false;

        if (inner.getYsize() > outer.getYsize())
            return false;

        return true;
    }

    public static Stream<BlockPos> getBlocksIn(MiniaturizationFieldSize fieldSize, int layerOffset) {
        AABB layerBounds = getLayerBounds(fieldSize, layerOffset);
        return getBlocksIn(layerBounds);
    }

    @Nonnull
    public static Stream<BlockPos> getBlocksIn(AABB bounds) {
        return BlockPos.betweenClosedStream(bounds.contract(1, 1, 1));
    }


    public static AABB getBoundsForBlocks(BlockPos[] filled) {
        return getBoundsForBlocks(Arrays.asList(filled));
    }

    public static AABB getBoundsForBlocks(Collection<BlockPos> filled) {
        if (filled.size() == 0)
            return AABB.ofSize(Vec3.ZERO,0, 0, 0);

        BoundingBox trimmedBounds = null;
        for (BlockPos filledPos : filled) {
            if (trimmedBounds == null) {
                trimmedBounds = BoundingBox.fromCorners(filledPos, filledPos);
                continue;
            }

            BoundingBox checkPos = BoundingBox.fromCorners(filledPos, filledPos);
            if (!trimmedBounds.intersects(checkPos))
                trimmedBounds.encapsulate(checkPos);
        }

        return AABB.of(trimmedBounds);
    }

    public static AABB getBoundsForBlocks(Stream<BlockPos> positions) {
        final Set<BlockPos> collect = positions.map(BlockPos::immutable).collect(Collectors.toSet());
        return getBoundsForBlocks(collect);
    }

    /**
     * Normalizes world coordinates to relative field coordinates.
     *
     * @param fieldBounds The bounds of the field itself.
     * @param pos         The position to normalize.
     * @return
     */
    public static BlockPos normalizeLayerPosition(AABB fieldBounds, BlockPos pos) {
        return new BlockPos(
                pos.getX() - fieldBounds.minX,
                pos.getY() - fieldBounds.minY,
                pos.getZ() - fieldBounds.minZ
        );
    }

    /**
     * Converts world-coordinate positions into relative field positions.
     *
     * @param fieldBounds    The boundaries of the crafting field.
     * @param fieldPositions The non-air block positions in the field (world coordinates).
     * @return
     */
    public static BlockPos[] normalizeLayerPositions(AABB fieldBounds, BlockPos[] fieldPositions) {
        // Normalize the block positions so the recipe can match easier
        return Stream.of(fieldPositions)
                .parallel()
                .map(p -> normalizeLayerPosition(fieldBounds, p))
                .map(BlockPos::immutable)
                .toArray(BlockPos[]::new);
    }

    public static BlockPos denormalizeLayerPosition(AABB realBounds, BlockPos normPos) {
        return new BlockPos(
                realBounds.minX + normPos.getX(),
                realBounds.minY + normPos.getY(),
                realBounds.minZ + normPos.getZ()
        );
    }

    public static AABB getCenterBounds(AABB bounds) {
        boolean xEven = bounds.getXsize() % 2 == 0;
        boolean yEven = bounds.getYsize() % 2 == 0;
        boolean zEven = bounds.getZsize() % 2 == 0;

        // Centers will either be at actual center of four blocks (even) or in center of single block (odd)
        double xExpansion = xEven ? 1 : 0.5d;
        double yExpansion = yEven ? 1 : 0.5d;
        double zExpansion = zEven ? 1 : 0.5d;

        return new AABB(bounds.getCenter(), bounds.getCenter())
                .inflate(xExpansion, yExpansion, zExpansion);
    }

    public static Stream<BlockPos> getCenterPositions(AABB bounds) {
        return getBlocksIn(getCenterBounds(bounds));
    }

    public static Stream<BlockPos> getInnerPositions(AABB bounds) {
        AABB insideBounds = bounds.contract(2, 0, 2).move(1, 0, 1);

        return BlockSpaceUtil.getBlocksIn(insideBounds);
    }

    public static Stream<BlockPos> getWallPositions(AABB bounds) {


        Set<BlockPos> allPositions = BlockSpaceUtil.getBlocksIn(bounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Set<BlockPos> insidePositions = getInnerPositions(bounds)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        allPositions.removeAll(insidePositions);
        return allPositions.stream();
    }

    public static BlockPos getOffset(AABB bounds) {
        return new BlockPos(bounds.minX, bounds.minY, bounds.maxZ);
    }

    public static Stream<BlockPos> getCornersOfBounds(AABB bounds) {
        boolean upperRequired = bounds.maxY > bounds.minY;
        Set<BlockPos> positions = new HashSet<>(upperRequired ? 8 : 4);

        // Lower corners
        positions.add(new BlockPos(bounds.minX, bounds.minY, bounds.minZ));
        positions.add(new BlockPos(bounds.minX, bounds.minY, bounds.maxZ - 1));
        positions.add(new BlockPos(bounds.maxX - 1, bounds.minY, bounds.minZ));
        positions.add(new BlockPos(bounds.maxX - 1, bounds.minY, bounds.maxZ - 1));

        if(upperRequired) {
            positions.add(new BlockPos(bounds.minX, bounds.maxY - 1, bounds.minZ));
            positions.add(new BlockPos(bounds.minX, bounds.maxY - 1, bounds.maxZ - 1));
            positions.add(new BlockPos(bounds.maxX - 1, bounds.maxY - 1, bounds.minZ));
            positions.add(new BlockPos(bounds.maxX - 1, bounds.maxY - 1, bounds.maxZ - 1));
        }

        return positions.stream();
    }

    public static Stream<BlockPos> getCornersOfBounds(MiniaturizationFieldSize fieldSize) {
        return getCornersOfBounds(fieldSize.getBoundsAtOrigin());
    }
}
