package com.robotgryphon.compactcrafting.projector.block;

import com.robotgryphon.compactcrafting.projector.ProjectorHelper;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

public class FieldProjectorBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public FieldProjectorBlock(Properties properties) {
        super(properties);

        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    public static Optional<Direction> getDirection(IWorldReader world, BlockPos position) {
        BlockState positionState = world.getBlockState(position);

        // The passed block position was not a field projector, cannot continue
        if (!(positionState.getBlock() instanceof FieldProjectorBlock))
            return Optional.empty();

        Direction facing = positionState.getValue(FieldProjectorBlock.FACING);
        return Optional.of(facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction looking = context.getHorizontalDirection();
        return defaultBlockState().setValue(FACING, looking);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FieldProjectorTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (world.isClientSide)
            return ActionResultType.SUCCESS;

        ProjectorHelper.getMissingProjectors(world, pos, state.getValue(FACING))
            .forEach(projPos -> spawnPlacementParticle((ServerWorld) world, projPos, ParticleTypes.BARRIER));

        // Uncomment for debug block placement
//        Arrays.stream(FieldProjectionSize.values()).forEach(size -> {
//            BlockPos center = size.getCenterFromProjector(pos, projectorFacing);
//
//            serverWorld.setBlock(center, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), 3);
//
//            size.getProjectorLocations(center).forEach(proj -> {
//                serverWorld.setBlock(proj.above(), Blocks.CYAN_STAINED_GLASS.defaultBlockState(), 3);
//            });
//        });

        return ActionResultType.SUCCESS;
    }

    private static void spawnPlacementParticle(ServerWorld world, BlockPos location, IParticleData particle) {
        if (world.getBlockState(location).getBlock() instanceof FieldProjectorBlock)
            return;

        world.sendParticles(particle,
                location.getX() + 0.5f,
                location.getY() + 0.5f,
                location.getZ() + 0.5f,
                1,
                0, 0, 0, 0);
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(worldIn.isClientSide)
            return;

        ServerWorld serverWorld = (ServerWorld) worldIn;

        FieldProjectorTile tile = (FieldProjectorTile) worldIn.getBlockEntity(pos);

        // If we don't have a valid field, search again
        if (tile == null)
            return;

        final Optional<BlockPos> firstMissingProjector = ProjectorHelper
                .getMissingProjectors(worldIn, pos, state.getValue(FACING))
                .findAny();

        // No missing projectors? Activate the field.
        if(!firstMissingProjector.isPresent()) {
            tile.tryActivateField();
        }

        // Add owner information to field projector
    }
}
