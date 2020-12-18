package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.*;
import java.util.stream.Stream;

/**
 * Contains utility methods for working with a set of projectors in a given space.
 */
public abstract class ProjectorHelper {

    /**
     * Given a world, initial projector position, and a field size, finds the center position for the field.
     *
     * @param world
     * @param initial
     * @param size
     * @return
     * @throws Exception
     */
    public static Optional<BlockPos> getCenterForSize(IWorldReader world, BlockPos initial, FieldProjectionSize size) {
        Optional<Direction> facing = FieldProjectorBlock.getDirection(world, initial);

        // Initial wasn't a valid field projector, can't get direction to look in
        if (!facing.isPresent())
            return Optional.empty();

        Direction fieldDirection = facing.get();

        BlockPos center = initial.offset(fieldDirection, size.getProjectorDistance() + 1);
        return Optional.of(center);
    }

    public static Optional<BlockPos> getCenterForSize(BlockPos initial, Direction facing, FieldProjectionSize size) {
        BlockPos center = initial.offset(facing, size.getProjectorDistance() + 1);
        return Optional.of(center);
    }

    public static Optional<BlockPos> getOppositePositionForSize(BlockPos initial, Direction direction, FieldProjectionSize size) {
        BlockPos center = initial.offset(direction, size.getProjectorDistance() + 1);
        BlockPos opp = center.offset(direction, size.getProjectorDistance() + 1);

        return Optional.of(opp);
    }

    public static Optional<BlockPos> getOppositePositionForSize(IWorldReader world, BlockPos initial, FieldProjectionSize size) {
        Optional<Direction> facing = FieldProjectorBlock.getDirection(world, initial);

        // Initial wasn't a valid field projector, can't get direction to look in
        if (!facing.isPresent())
            return Optional.empty();

        Direction fieldDirection = facing.get();
        return getOppositePositionForSize(initial, fieldDirection, size);
    }

    public static Optional<FieldProjectionSize> getClosestOppositeSize(IWorldReader world, BlockPos initial) {
        for (FieldProjectionSize size : FieldProjectionSize.values()) {
            if (hasProjectorOpposite(world, initial, size)) {
                return Optional.of(size);
            }
        }

        return Optional.empty();
    }

    public static Optional<FieldProjectionSize> getClosestOppositeSize(IWorldReader world, BlockPos initial, Direction look) {
        for (FieldProjectionSize size : FieldProjectionSize.values()) {
            if (hasProjectorOpposite(world, initial, look, size)) {
                return Optional.of(size);
            }
        }

        return Optional.empty();
    }

    public static Set<BlockPos> getProjectorLocationsForAxis(BlockPos center, Direction.Axis axis, FieldProjectionSize size) {
        Direction posdir = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis);
        BlockPos posLocation = ProjectorHelper.getProjectorLocationForDirection(center, posdir, size);
        BlockPos negLocation = ProjectorHelper.getProjectorLocationForDirection(center, posdir.getOpposite(), size);

        return new HashSet<>(Arrays.asList(posLocation, negLocation));
    }

    public static BlockPos getProjectorLocationForDirection(BlockPos center, Direction direction, FieldProjectionSize size) {
        BlockPos location = center.offset(direction, size.getProjectorDistance() + 1);
        return location;
    }

    /**
     * Checks to see if there is a valid field projector in a given direction, given a field size.
     *
     * @param world     The world to search in.
     * @param center    The center of the projection field.
     * @param direction The direction to check.
     * @param size      The field size to check.
     * @return
     */
    public static boolean hasProjectorInDirection(IWorldReader world, BlockPos center, Direction direction, FieldProjectionSize size) {
        BlockPos location = getProjectorLocationForDirection(center, direction, size);
        BlockState state = world.getBlockState(location);

        if (state.getBlock() instanceof FieldProjectorBlock) {
            Direction projectorFacing = state.get(FieldProjectorBlock.FACING);
            return projectorFacing.getOpposite() == direction;
        }

        return false;
    }

    public static boolean hasProjectorOpposite(IWorldReader world, BlockPos initial, FieldProjectionSize size) {
        Optional<BlockPos> opp = getOppositePositionForSize(world, initial, size);
        return opp
                .map(possible -> world.getBlockState(possible).getBlock() instanceof FieldProjectorBlock)
                .orElse(false);
    }

    public static boolean hasProjectorOpposite(IWorldReader world, BlockPos initial, Direction look, FieldProjectionSize size) {
        Optional<BlockPos> opp = getOppositePositionForSize(initial, look, size);
        return opp
                .map(possible -> world.getBlockState(possible).getBlock() instanceof FieldProjectorBlock)
                .orElse(false);
    }

    /**
     * Checks an axis to see if, given a field size, there are two valid projectors.
     *
     * @param world       The world to search in.
     * @param center      The center of the projection field.
     * @param primaryAxis The axis to check projectors on. Must be a horizontal axis.
     * @param fieldSize   A potential size to check the cross axis for projectors.
     * @return
     */
    public static boolean checkAxisForValidProjectors(IWorldReader world, BlockPos center, Direction.Axis primaryAxis, FieldProjectionSize fieldSize) {
        if (primaryAxis.isVertical())
            return false;

        Direction checkDirection = Direction.getFacingFromAxisDirection(primaryAxis, Direction.AxisDirection.POSITIVE);

        // Do we have a valid projector in the first direction?
        boolean posValid = hasProjectorInDirection(world, center, checkDirection, fieldSize);
        if (!posValid)
            return false;

        // What about the negative direction?
        boolean negValid = hasProjectorInDirection(world, center, checkDirection.getOpposite(), fieldSize);
        if (!negValid)
            return false;

        // We have two valid projectors for this size and axis
        return true;
    }

    /**
     * Performs a world check to see if, given a field projection size, that all four projectors exist
     * and are in a valid state (present, facing towards center block).
     *
     * @param world
     * @param center
     * @param fieldSize
     * @return
     */
    public static boolean checkProjectorsValid(IWorldReader world, BlockPos center, FieldProjectionSize fieldSize) {
        boolean xAxis = checkAxisForValidProjectors(world, center, Direction.Axis.X, fieldSize);
        if (!xAxis)
            return false;

        boolean zAxis = checkAxisForValidProjectors(world, center, Direction.Axis.Z, fieldSize);
        return zAxis;
    }

    public static Stream<BlockPos> getValidOppositePositions(IWorldReader world, BlockPos initial) {
        Stream<BlockPos> validOpposites = Stream.of(FieldProjectionSize.values())
                .map(s -> getOppositePositionForSize(world, initial, s))
                .filter(Optional::isPresent)
                .map(Optional::get);

        return validOpposites;
    }


}
