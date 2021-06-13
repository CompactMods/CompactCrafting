package com.robotgryphon.compactcrafting.field;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.stream.Stream;

public enum FieldProjectionSize {
    /**
     * 3x3x3 Crafting Field Size (Magnitude 1)
     */
    SMALL(1, 3, "small"),

    /**
     * 5x5x5 Crafting Field Size (Magnitude 2)
     */
    MEDIUM(2, 5, "medium"),

    /**
     * 7x7x7 Crafting Field Size (Magnitude 3)
     */
    LARGE(3, 7, "large"),

    ABSURD(4, 9, "absurd");

    private int size;

    /**
     * Number of blocks between two projectors.
     */
    private int projectorDistance;

    private String name;

    FieldProjectionSize(int size, int distance, String name) {
        this.size = size;
        this.projectorDistance = distance;
        this.name = name;
    }

    /**
     * Gets the distance between the center of a field and a projector. (exclusive)
     *
     * @return
     */
    public int getProjectorDistance() {
        return this.projectorDistance;
    }

    public int getDimensions() {
        return (this.size * 2) + 1;
    }

    public int getSize() {
        return this.size;
    }

    public String getName() {
        return this.name;
    }

    public static FieldProjectionSize maximum() {
        return LARGE;
    }

    public BlockPos getCenterFromProjector(BlockPos projector, Direction facing) {
        return projector.relative(facing, this.getProjectorDistance() + 1);
    }

    public BlockPos getProjectorLocationForDirection(BlockPos center, Direction direction) {
        return center.relative(direction, this.getProjectorDistance() + 1);
    }

    public Stream<BlockPos> getProjectorLocations(BlockPos center) {
        return Arrays
                .stream(new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST})
                .filter(d -> d.getAxis().isHorizontal())
                .map(hor -> getProjectorLocationForDirection(center, hor));
    }

    public Stream<BlockPos> getProjectorLocationsForAxis(BlockPos center, Direction.Axis axis) {
        Direction posdir = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos posLocation = getProjectorLocationForDirection(center, posdir);
        BlockPos negLocation = getProjectorLocationForDirection(center, posdir.getOpposite());

        return Stream.of(posLocation, negLocation);
    }

    public BlockPos getOppositeProjectorPosition(BlockPos projectorPos, Direction projectorFacing) {
        BlockPos center = getCenterFromProjector(projectorPos, projectorFacing);
        return getProjectorLocationForDirection(center, projectorFacing);
    }
}
