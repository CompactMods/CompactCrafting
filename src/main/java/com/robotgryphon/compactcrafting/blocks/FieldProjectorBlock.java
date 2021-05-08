package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import com.robotgryphon.compactcrafting.tiles.DummyFieldProjectorTile;
import com.robotgryphon.compactcrafting.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.tiles.MainFieldProjectorTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

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
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        FieldProjectorTile tile = (FieldProjectorTile) worldIn.getBlockEntity(pos);
        if (tile == null)
            return;

        tile.getMainProjectorTile().ifPresent(MainFieldProjectorTile::doRecipeScan);
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
        BlockState base = super.getStateForPlacement(context);
        Direction looking = context.getHorizontalDirection();

        // Place facing towards the player
        return base.setValue(FACING, looking.getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        Direction fac = state.getValue(FACING);
        return fac == Direction.SOUTH ? new MainFieldProjectorTile() : new DummyFieldProjectorTile();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (world.isClientSide)
            return ActionResultType.SUCCESS;

        FieldProjectorTile tile = (FieldProjectorTile) world.getBlockEntity(pos);

        // Shouldn't happen, but safety
        if (tile == null)
            return ActionResultType.PASS;


        Optional<FieldProjectionSize> fieldSize = ProjectorHelper.getClosestOppositeSize(world, pos);
        if (!fieldSize.isPresent()) {
            // Spawn particle in valid places
            ProjectorHelper.getValidOppositePositions(world, pos)
                    .forEach(opp -> spawnPlacementParticle((ServerWorld) world, opp));

        } else {
            FieldProjectionSize size = fieldSize.get();

            Optional<BlockPos> centerForSize = ProjectorHelper.getCenterForSize(world, pos, size);
            centerForSize.ifPresent(center -> {
                Direction.Axis a = state.getValue(FACING).getAxis();
                Direction.Axis opp;
                switch (a) {
                    case X:
                        opp = Direction.Axis.Z;
                        break;

                    case Z:
                        opp = Direction.Axis.X;
                        break;

                    default:
                        return;
                }

                ProjectorHelper.getProjectorLocationsForAxis(center, opp, size)
                        .forEach(loc -> spawnPlacementParticle((ServerWorld) world, loc));
            });

        }
        return ActionResultType.SUCCESS;
    }

    private static void spawnPlacementParticle(ServerWorld world, BlockPos opp) {
        if (world.getBlockState(opp).getBlock() instanceof FieldProjectorBlock)
            return;

        world.sendParticles(ParticleTypes.BARRIER,
                opp.getX() + 0.5f,
                opp.getY() + 0.5f,
                opp.getZ() + 0.5f,
                1,
                0, 0, 0, 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        FieldProjectorTile tile = (FieldProjectorTile) worldIn.getBlockEntity(pos);

        // If we don't have a valid field, search again
        if(tile == null)
            return;

        Optional<MainFieldProjectorTile> previousMain = tile.getMainProjectorTile();
        if(tile instanceof DummyFieldProjectorTile) {

            DummyFieldProjectorTile dummy = (DummyFieldProjectorTile) tile;
            dummy.onInitialPlacement();
        }

        // Check the old one too just in case (e.g. wrenches)
        previousMain.ifPresent(MainFieldProjectorTile::doFieldCheck);
        tile.getMainProjectorTile().ifPresent(MainFieldProjectorTile::doFieldCheck);

        // Add owner information to field projector
    }
}
