package com.robotgryphon.compactcrafting.field;

import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Contains utility methods for working with a set of projectors in a given space.
 */
public class ProjectorHelper {
    private ProjectorHelper() {}

    public static Stream<BlockPos> getPossibleCenters(IWorldReader world, BlockPos initial, Direction facing) {
        return Stream.of(FieldProjectionSize.values())
                .filter(s -> hasProjectorOpposite(world, initial, facing, s))
                .map(size -> getCenterForSize(initial, facing, size))
                .map(Optional::get);
    }

    public static Stream<BlockPos> getPossibleMainProjectors(IWorldReader world, BlockPos initial, Direction facing) {
        return getPossibleCenters(world, initial, facing)
                .flatMap(center -> getProjectorsInDirection(world, center, Direction.NORTH));
    }

    private static boolean hasProjectorInDirection(IWorldReader world, BlockPos center, Direction direction) {
        return getProjectorsInDirection(world, center, direction).findAny().isPresent();
    }

    private static Stream<BlockPos> getProjectorsInDirection(IWorldReader world, BlockPos center, Direction direction) {
        Set<BlockPos> positions = new HashSet<>();
        for (FieldProjectionSize fieldSize : FieldProjectionSize.values()) {
            BlockPos possibleLocation = getProjectorLocationForDirection(center, direction, fieldSize);
            if (!hasProjectorInPositionForDirection(world, direction, possibleLocation))
                continue;

            positions.add(possibleLocation);
        }

        return positions.stream();
    }

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

        BlockPos center = initial.relative(fieldDirection, size.getProjectorOffset());
        return Optional.of(center);
    }

    public static Optional<BlockPos> getCenterForSize(BlockPos initial, Direction facing, FieldProjectionSize size) {
        BlockPos center = initial.relative(facing, size.getProjectorOffset());
        return Optional.of(center);
    }

    public static Optional<BlockPos> getOppositePositionForSize(BlockPos initial, Direction facing, FieldProjectionSize size) {
        BlockPos center = initial.relative(facing, size.getProjectorOffset());
        BlockPos opp = center.relative(facing, size.getProjectorOffset());

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

    public static Stream<BlockPos> getProjectorLocations(BlockPos center, FieldProjectionSize fieldSize) {
        return Arrays
                .stream(new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST})
                .filter(d -> d.getAxis().isHorizontal())
                .map(hor -> getProjectorLocationForDirection(center, hor, fieldSize));
    }

    public static Set<BlockPos> getProjectorLocationsForAxis(BlockPos center, Direction.Axis axis, FieldProjectionSize size) {
        Direction posdir = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos posLocation = ProjectorHelper.getProjectorLocationForDirection(center, posdir, size);
        BlockPos negLocation = ProjectorHelper.getProjectorLocationForDirection(center, posdir.getOpposite(), size);

        return new HashSet<>(Arrays.asList(posLocation, negLocation));
    }

    public static BlockPos getProjectorLocationForDirection(BlockPos center, Direction direction, FieldProjectionSize size) {
        BlockPos location = center.relative(direction, size.getProjectorOffset());
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
        return hasProjectorInPositionForDirection(world, direction, location);
    }

    public static boolean hasProjectorInPosition(IWorldReader world, BlockPos location) {
        BlockState state = world.getBlockState(location);

        return (state.getBlock() instanceof FieldProjectorBlock);
    }

    private static boolean hasProjectorInPositionForDirection(IWorldReader world, Direction direction, BlockPos location) {
        if (!hasProjectorInPosition(world, location))
            return false;

        return FieldProjectorBlock.getDirection(world, location)
                .map(fd -> fd == direction.getOpposite())
                .orElse(false);
    }

    public static boolean hasProjectorOpposite(IWorldReader world, BlockPos initial, FieldProjectionSize size) {
        Optional<BlockPos> opp = getOppositePositionForSize(world, initial, size);
        return opp
                .map(possible -> world.getBlockState(possible).getBlock() instanceof FieldProjectorBlock)
                .orElse(false);
    }

    public static boolean hasProjectorOpposite(IWorldReader world, BlockPos initial, Direction facing, FieldProjectionSize size) {
        Optional<BlockPos> opp = getOppositePositionForSize(initial, facing, size);
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

        Direction checkDirection = Direction.fromAxisAndDirection(primaryAxis, Direction.AxisDirection.POSITIVE);

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
        return Stream.of(FieldProjectionSize.values())
                .map(s -> getOppositePositionForSize(world, initial, s))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


    public static Optional<FieldProjectionSize> findSizeByMainProjector(BlockPos fieldCenter, BlockPos aProjector) {
        return Arrays.stream(FieldProjectionSize.values())
                .filter(size -> {
                    // Try to match the positions, if so we found a valid size
                    BlockPos location = fieldCenter.relative(Direction.NORTH, size.getProjectorOffset());
                    return location == aProjector;
                }).findFirst();

    }
}
