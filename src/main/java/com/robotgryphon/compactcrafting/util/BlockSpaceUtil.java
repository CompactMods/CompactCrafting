package com.robotgryphon.compactcrafting.util;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
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

    public static BlockPos[] rotateLayerPositions(BlockPos[] positions, Vector3i fieldCenter) {
        return Stream.of(positions)
                .map(p -> p.subtract(fieldCenter))
                .map(p -> p.rotate(Rotation.CLOCKWISE_90))
                .map(p -> p.add(fieldCenter))
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
}
