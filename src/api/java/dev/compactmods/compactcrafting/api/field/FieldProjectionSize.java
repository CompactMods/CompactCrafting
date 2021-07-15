package dev.compactmods.compactcrafting.api.field;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum FieldProjectionSize implements IStringSerializable {
    /**
     * Inactive field. Does not have dimensions.
     */
    INACTIVE("inactive", 0, 0),

    /**
     * 3x3x3 crafting field Size
     */
    SMALL("small", 1, 3),

    /**
     * 5x5x5 crafting field Size
     */
    MEDIUM("medium", 2, 5),

    /**
     * 7x7x7 crafting field Size
     */
    LARGE("large", 3, 7),

    /**
     * 9x9x9 crafting field size.
     */
    ABSURD("absurd", 4, 9);

    private final int size;

    /**
     * Number of blocks between two projectors.
     */
    private final int projectorDistance;

    private final String name;

    public static final FieldProjectionSize[] VALID_SIZES = new FieldProjectionSize[] {
            SMALL, MEDIUM, LARGE, ABSURD
    };

    FieldProjectionSize(String name, int size, int distance) {
        this.size = size;
        this.projectorDistance = distance;
        this.name = name;
    }

    @Nonnull
    public static Optional<FieldProjectionSize> fromDimensions(double size) {
        // smaller than small, larger than max size, or not an odd size
        if(size < SMALL.getDimensions() || size > maximum().getDimensions() || size % 2 == 0)
            return Optional.empty();

        return Arrays.stream(values())
                .filter(s -> s.getDimensions() == size)
                .findFirst();
    }

    public static boolean canFitDimensions(int dims) {
        return dims >= 1 && dims <= ABSURD.getDimensions();
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
        return ABSURD;
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

    public AxisAlignedBB getBoundsAtPosition(BlockPos center) {
        return new AxisAlignedBB(center).inflate(this.size);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
