package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.projector.world.ProjectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public abstract class FieldProjectorBlock extends Block implements ProjectorBlock {

    protected FieldProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter levelReader, @NonNull BlockPos pos, @NonNull CollisionContext ctx) {
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        final var north = Shapes.or(BASE, POLE, DISH_NORTH).optimize();
        final var all = Shapes.rotateAll(north);
        return switch (dir) {
            case WEST -> Shapes.or(BASE, POLE, DISH_WEST);
            case NORTH -> Shapes.or(BASE, POLE, DISH_NORTH);
            case EAST -> Shapes.or(BASE, POLE, DISH_EAST);
            case SOUTH -> Shapes.or(BASE, POLE, DISH_SOUTH);
            default -> Shapes.or(BASE, POLE);
        };
    }

    @Override
    protected @NonNull VoxelShape getOcclusionShape(@NonNull BlockState state) {
        return Shapes.empty();
    }
}
