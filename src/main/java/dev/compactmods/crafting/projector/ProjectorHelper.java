package dev.compactmods.crafting.projector;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.DirectionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * Contains utility methods for working with a set of projectors in a given space.
 */
public abstract class ProjectorHelper {
    public static Optional<MiniaturizationFieldSize> getClosestSize(IBlockReader level, BlockPos initial, Direction facing) {
        final Optional<MiniaturizationFieldSize> opposing = getClosestOppositeSize(level, initial, facing);
        if(opposing.isPresent()) return opposing;

        return getSmallestCrossAxisSize(level, initial, facing);
    }

    public static Optional<MiniaturizationFieldSize> getClosestOppositeSize(IBlockReader level, BlockPos initial, Direction facing) {
        return Stream.of(MiniaturizationFieldSize.VALID_SIZES)
                .filter(size -> {
                    BlockPos oppPos = size.getOppositeProjectorPosition(initial, facing);
                    BlockState oppState = level.getBlockState(oppPos);
                    if(oppState.getBlock() instanceof FieldProjectorBlock) {
                        Direction f = oppState.getValue(FieldProjectorBlock.FACING);
                        return f.getOpposite().equals(facing);
                    }

                    return false;
                })
                .findFirst();
    }

    public static Optional<MiniaturizationFieldSize> getClosestOppositeSize(IBlockReader world, BlockPos initial) {
        for (MiniaturizationFieldSize size : MiniaturizationFieldSize.VALID_SIZES) {
            if (hasProjectorOpposite(world, initial, size)) {
                return Optional.of(size);
            }
        }

        return Optional.empty();
    }

    /**
     * Queries the world for the projector direction, then tries to find a projector on the
     * opposing side. If it finds a projector block, it checks it's facing towards the same
     * center as the first projector.
     *
     * @param world Level reader for blockstate information.
     * @param initial The position the initial projector is in.
     * @param size The size to check for an opposing projector at.
     *
     * @return True if there is an opposing projector facing the same center as the initial position.
     */
    public static boolean hasProjectorOpposite(IBlockReader world, BlockPos initial, MiniaturizationFieldSize size) {
        Optional<Direction> initialFacing = FieldProjectorBlock.getDirection(world, initial);

        // Initial wasn't a valid field projector, can't get direction to look in
        if (!initialFacing.isPresent())
            return false;

        Direction initFacing = initialFacing.get();

        BlockPos oppositePos = size.getOppositeProjectorPosition(initial, initFacing);

        BlockState oppositeState = world.getBlockState(oppositePos);
        if(oppositeState.getBlock() instanceof FieldProjectorBlock) {
            Direction oppFacing = oppositeState.getValue(FieldProjectorBlock.FACING);
            if(oppFacing.getOpposite() == initFacing)
                return true;
        }

        return false;
    }

    public static Stream<BlockPos> getValidOppositePositions(BlockPos initial, Direction facing) {
        return Stream.of(MiniaturizationFieldSize.VALID_SIZES)
                .map(s -> s.getOppositeProjectorPosition(initial, facing));
    }

    public static boolean hasValidCrossProjector(IBlockReader world, BlockPos initialProjector, Direction projectorFacing, MiniaturizationFieldSize size) {
        Direction.Axis crossAxis = DirectionUtil.getCrossDirectionAxis(projectorFacing.getAxis());

        // Filter by at least one valid cross-axis projector
        BlockPos sizeCenter = size.getCenterFromProjector(initialProjector, projectorFacing);

        // 26, 57, 6 = SMALL center
        // 26, 57, 2 = NORTH
        // 26, 57, 10 = SOUTH

        return size.getProjectorLocationsForAxis(sizeCenter, crossAxis)
                .anyMatch(crossProjPos -> {
                    BlockState crossProjState = world.getBlockState(crossProjPos);

                    if (!(crossProjState.getBlock() instanceof FieldProjectorBlock))
                        return false;

                    Direction crossFacing = crossProjState.getValue(FieldProjectorBlock.FACING);
                    BlockPos crossFacingCenter = size.getCenterFromProjector(crossProjPos, crossFacing);

                    return crossFacingCenter.equals(sizeCenter);
                });
    }

    public static Stream<BlockPos> getMissingProjectors(IBlockReader level, BlockPos initialProjector, Direction projectorFacing) {
        Optional<MiniaturizationFieldSize> fieldSize = ProjectorHelper.getClosestOppositeSize(level, initialProjector);

        // If we have a field size, an opposing projector was found
        // Just show particles where to place the two projectors on the cross axis
        if (fieldSize.isPresent()) {
            MiniaturizationFieldSize size = fieldSize.get();
            BlockPos center = size.getCenterFromProjector(initialProjector, projectorFacing);

            return getMissingProjectors(level, size, center);
        } else {
            // No opposing projector to limit field size.
            // Scan for a cross-axis projector to try to limit.
            Optional<MiniaturizationFieldSize> firstMatchedSize = getSmallestCrossAxisSize(level, initialProjector, projectorFacing);

            if (firstMatchedSize.isPresent()) {
                // One of the projectors on the cross axis were valid
                MiniaturizationFieldSize matchedSize = firstMatchedSize.get();

                BlockPos matchedCenter = matchedSize.getCenterFromProjector(initialProjector, projectorFacing);
                return ProjectorHelper.getMissingProjectors(level, matchedSize, matchedCenter);
            } else {
                // Need an opposing projector set up to limit size
                return ProjectorHelper.getValidOppositePositions(initialProjector, projectorFacing);
            }
        }
    }

    @Nonnull
    private static Optional<MiniaturizationFieldSize> getSmallestCrossAxisSize(IBlockReader level, BlockPos initial, Direction facing) {
        return Stream.of(MiniaturizationFieldSize.VALID_SIZES)
                .filter(size -> hasValidCrossProjector(level, initial, facing, size))
                .findFirst();
    }

    @Nonnull
    public static Stream<BlockPos> getMissingProjectors(IBlockReader level, MiniaturizationFieldSize size, BlockPos center) {
        return size.getProjectorLocations(center)
                // inverted filter - if the projector doesn't point to the center or isn't a projector, add to list
                .filter(proj -> !projectorFacesCenter(level, proj, center, size));
    }

    public static boolean projectorFacesCenter(IBlockReader world, BlockPos proj, BlockPos actualCenter, MiniaturizationFieldSize size) {
        return FieldProjectorBlock.getDirection(world, proj)
                .map(projFacing -> size.getCenterFromProjector(proj, projFacing).equals(actualCenter))
                .orElse(false);
    }
}
