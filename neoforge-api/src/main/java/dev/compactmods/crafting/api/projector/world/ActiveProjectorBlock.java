package dev.compactmods.crafting.api.projector.world;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.api.projector.FieldProjectorProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface ActiveProjectorBlock extends ProjectorBlock {

    default MiniaturizationFieldSize fieldSize(BlockState state) {
        return state.getValueOrElse(FieldProjectorProperties.SIZE, MiniaturizationFieldSize.SMALL);
    }

    default MiniaturizationFieldLocation fieldLocation(BlockPos position, BlockState state) {
        final var pos = ProjectorBlock.placement(position, state);
        final var size = fieldSize(state);
        return MiniaturizationFieldLocation.compute(size, pos);
    }
}
