package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FieldProjection {

    private FieldProjectionSize size;
    private BlockPos center;

    private FieldProjection(FieldProjectionSize size, BlockPos center) {
        this.center = center;
        this.size = size;
    }

    public FieldProjectionSize getFieldSize() {
        return this.size;
    }

    public BlockPos getCenterPosition() {
        return center;
    }

    public Optional<AxisAlignedBB> getBounds() {
        FieldProjectionSize size = this.size;
        BlockPos center = getCenterPosition();
        AxisAlignedBB bounds = new AxisAlignedBB(center).grow(size.getSize());

        return Optional.of(bounds);
    }

    /**
     * Fetches a set of potential field projection sizes via an initial projector location. Will offset to a
     * set of center blocks, then validate if there are all four projectors at that size.
     *
     * @param world
     * @param initial
     * @return
     */
    public static Set<FieldProjectionSize> getValidFieldSizesByInitial(IWorldReader world, BlockPos initial) {
        Optional<Direction> facing = FieldProjectorBlock.getDirection(world, initial);

        // Initial wasn't a valid field projector, can't get direction to look in
        if (!facing.isPresent())
            return Collections.emptySet();

        Direction fieldDirection = facing.get();

        return Stream.of(FieldProjectionSize.values())
                .filter(s -> isFieldSizeValid(world, initial, fieldDirection, s))
                .collect(Collectors.toSet());
    }

    private static boolean isFieldSizeValid(IWorldReader world, BlockPos initial, Direction fieldDirection, FieldProjectionSize size) {
        // check block exists in offset position
        Optional<BlockPos> center = ProjectorHelper.getCenterForSize(world, initial, size);

        // No center block or couldn't get projector direction
        if(!center.isPresent())
            return false;

        return ProjectorHelper.checkProjectorsValid(world, center.get(), size);
    }

    public static Optional<FieldProjection> tryCreateFromPosition(IWorldReader world, BlockPos position) {
        Optional<Direction> dir = FieldProjectorBlock.getDirection(world, position);

        // No direction found - probably not a field projector at this location
        if(!dir.isPresent())
            return Optional.empty();

        Set<FieldProjectionSize> potentialSizes = getValidFieldSizesByInitial(world, position);

        for (FieldProjectionSize potSize : potentialSizes) {
            Optional<BlockPos> center = ProjectorHelper.getCenterForSize(world, position, potSize);
            if(!center.isPresent())
                continue;

            Direction.Axis mainAxis = dir.get().getAxis();
            Direction.Axis crossAxis;
            switch(mainAxis) {
                case X:
                    crossAxis = Direction.Axis.Z;
                    break;

                case Z:
                    crossAxis = Direction.Axis.X;
                    break;

                default:
                    // Thi shouldn't happen, but better than an error
                    crossAxis = Direction.Axis.Y;
                    break;
            }

            boolean crossAxisValid = ProjectorHelper.checkAxisForValidProjectors(world, center.get(), crossAxis, potSize);

            // Found a valid size?
            if(crossAxisValid)
                return Optional.of(new FieldProjection(potSize, center.get()));
        }

        // No cross axis tests were successful
        return Optional.empty();
    }

    public BlockPos getProjectorInDirection(Direction direction) {
        return center.offset(direction, size.getProjectorDistance());
    }
}
