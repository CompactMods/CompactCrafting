package dev.compactmods.crafting.api.projector.placement;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.FieldProjectorPredicates;
import dev.compactmods.crafting.api.projector.FieldProjectorTags;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;

import java.util.Arrays;
import java.util.stream.Stream;

public record ProjectorPlacement(GlobalPos position, Direction facing) {

    public static ProjectorPlacement compute(BlockPlaceContext context) {
        Direction placeDirection = context.getHorizontalDirection();

        // Hold shift to make the projector face you; else face away
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            placeDirection = placeDirection.getOpposite();

        final var pos = GlobalPos.of(
                context.getLevel().dimension(),
                context.getClickedPos().immutable()
        );

        return new ProjectorPlacement(pos, placeDirection);
    }

    public static ProjectorPlacement compute(MiniaturizationFieldLocation fieldLocation, Direction direction) {
        final var blockCenter = fieldLocation.centerBlock();
        final var location = blockCenter.relative(direction, fieldLocation.size().getProjectorDistance() + 1);
        return new ProjectorPlacement(GlobalPos.of(fieldLocation.dimension(), location.immutable()), direction.getOpposite());
    }

    public ProjectorPlacement getMirror(MiniaturizationFieldLocation fieldLocation) {
        return compute(fieldLocation, facing.getOpposite());
    }

    public Stream<ProjectorPlacement> getPossibleMirrors() {
        return Arrays.stream(MiniaturizationFieldSize.values())
                .map(size -> MiniaturizationFieldLocation.compute(this.position.dimension(), size, this))
                .map(field -> compute(field, facing.getOpposite()));
    }

    public Stream<MiniaturizationFieldLocation> getPossibleFields() {
        return Arrays.stream(MiniaturizationFieldSize.values())
                .map(size -> MiniaturizationFieldLocation.compute(this.position.dimension(), size, this));
    }

    public boolean isProjector(LevelReader level) {
        return level.getBlockState(position.pos()).is(FieldProjectorTags.PROJECTOR_BLOCK);
    }

    public boolean isActive(LevelReader level) {
        final var state = level.getBlockState(position.pos());
        return state.is(FieldProjectorTags.PROJECTOR_BLOCK, FieldProjectorPredicates.IS_ACTIVE);
    }
}
