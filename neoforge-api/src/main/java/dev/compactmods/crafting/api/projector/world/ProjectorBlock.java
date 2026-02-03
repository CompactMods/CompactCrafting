package dev.compactmods.crafting.api.projector.world;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.projector.FieldProjectorPredicates;
import dev.compactmods.crafting.api.projector.FieldProjectorProperties;
import dev.compactmods.crafting.api.projector.FieldProjectorTags;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface ProjectorBlock {

    VoxelShape BASE = Shapes.box(0, 0, 0, 1, 6 / 16d, 1);

    VoxelShape POLE = Shapes.box(7 / 16d, 6 / 16d, 7 / 16d, 9 / 16d, 12 / 16d, 9 / 16d);

    VoxelShape DISH_WEST = Shapes.box(3 / 16d, 0.5d, 3 / 16d,
            7 / 16d, 1, 13 / 16d);

    VoxelShape DISH_EAST = Shapes.box(9 / 16d, 0.5d, 3 / 16d,
            13 / 16d, 1, 13 / 16d);

    VoxelShape DISH_NORTH = Shapes.box(3 / 16d, 0.5d, 3 / 16d,
            13 / 16d, 1, 7 / 16d);

    VoxelShape DISH_SOUTH = Shapes.box(3 / 16d, 0.5d, 9 / 16d,
            13 / 16d, 1, 13 / 16d);

    static Direction facing(BlockState state) {
        return state.getValueOrElse(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
    }

    static ProjectorPlacement placement(GlobalPos position, BlockState state) {
        return new ProjectorPlacement(position, facing(state));
    }

    static MiniaturizationFieldSize fieldSize(BlockState state) {
        return state.is(FieldProjectorTags.ACTIVE_PROJECTOR_BLOCK, FieldProjectorPredicates.IS_ACTIVE)
                ? state.getValue(FieldProjectorProperties.SIZE) : null;
    }
}
