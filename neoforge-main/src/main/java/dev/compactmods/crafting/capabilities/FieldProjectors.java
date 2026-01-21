package dev.compactmods.crafting.capabilities;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.ActiveProjectorInfo;
import dev.compactmods.crafting.api.projector.FieldProjectorPredicates;
import dev.compactmods.crafting.api.projector.FieldProjectorTags;
import dev.compactmods.crafting.api.projector.capability.FieldProjectorController;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import dev.compactmods.crafting.api.projector.world.ProjectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public interface FieldProjectors {

    @Nullable
    static FieldProjectorController resolve(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Void unused) {
        boolean isProjector = state.is(FieldProjectorTags.PROJECTOR_BLOCK);
        if(!isProjector)
            return null;

        final var placement = ProjectorBlock.placement(pos, state);
        if(state.is(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK, FieldProjectorPredicates.IS_ACTIVE)) {
            return ActiveProjectorInfo.from(pos, state)
                    .map(info -> new ActiveFieldProjector(level, info))
                    .orElse(null);
        }

        if(state.is(FieldProjectorTags.INACTIVE_PROJECTOR_BLOCK, FieldProjectorPredicates.IS_INACTIVE)) {
            return new InactiveFieldProjector(level, placement);
        }

        return null;
    }

    record InactiveFieldProjector(Level level, ProjectorPlacement placement) implements FieldProjectorController {

        @Override
        public Optional<MiniaturizationFieldLocation> fieldLocation() {
            return Optional.empty();
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean activate() {
            return false;
        }

        @Override
        public boolean deactivate() {
            return true;
        }
    }

    record ActiveFieldProjector(Level level, ActiveProjectorInfo projectorInfo) implements FieldProjectorController {

        @Override
        public ProjectorPlacement placement() {
            return projectorInfo.placement();
        }

        @Override
        public Optional<MiniaturizationFieldLocation> fieldLocation() {
            return Optional.of(projectorInfo.target());
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean activate() {
            return true;
        }

        @Override
        public boolean deactivate() {
            return false;
        }
    }
}
