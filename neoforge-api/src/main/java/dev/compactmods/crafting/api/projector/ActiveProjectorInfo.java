package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;

public record ActiveProjectorInfo(ProjectorPlacement placement, MiniaturizationFieldLocation target) {

    public static <T extends LevelReader> Optional<ActiveProjectorInfo> from(T level, BlockPos position) {
        final var state = level.getBlockState(position);
        return from(position, state);
    }

    public static Optional<ActiveProjectorInfo> from(BlockPos position, BlockState state) {
        if(!state.is(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK))
            return Optional.empty();

        // Placement
        final var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        final var placement = new ProjectorPlacement(position, facing);

        // Field Location
        final var fieldSize = state.getValue(FieldProjectorProperties.SIZE);
        final var fieldLocation = MiniaturizationFieldLocation.compute(fieldSize, placement);

        return Optional.of(new ActiveProjectorInfo(placement, fieldLocation));
    }
}
