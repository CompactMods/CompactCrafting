package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

//import mcjty.theoneprobe.api.IProbeHitData;
//import mcjty.theoneprobe.api.IProbeInfo;
//import mcjty.theoneprobe.api.IProbeInfoProvider;
//import mcjty.theoneprobe.api.ProbeMode;

public class FieldProjectorBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public FieldProjectorBlock(Properties properties) {
        super(properties);

        setDefaultState(getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH));
    }

    public static Optional<Direction> getDirection(IWorldReader world, BlockPos position) {
        BlockState positionState = world.getBlockState(position);

        // The passed block position was not a field projector, cannot continue
        if (!(positionState.getBlock() instanceof FieldProjectorBlock))
            return Optional.empty();

        Direction facing = positionState.get(FieldProjectorBlock.FACING);
        return Optional.of(facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        FieldProjectorTile tile = (FieldProjectorTile) worldIn.getTileEntity(pos);
        if (tile == null)
            return;

        tile.doRecipeScan();
    }


    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
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
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (world.isRemote)
            return ActionResultType.SUCCESS;

        FieldProjectorTile tile = (FieldProjectorTile) world.getTileEntity(pos);

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
                Direction.Axis a = state.get(FACING).getAxis();
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
        if(world.getBlockState(opp).getBlock() instanceof FieldProjectorBlock)
            return;

        world.spawnParticle(ParticleTypes.BARRIER,
                opp.getX() + 0.5f,
                opp.getY() + 0.5f,
                opp.getZ() + 0.5f,
                1,
                0, 0, 0, 0);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity
            placer, ItemStack stack) {
        Direction facing = placer.getHorizontalFacing();

        worldIn.setBlockState(pos, state.with(FACING, facing));

        // Add owner information to field projector
    }

    // TODO: Finish in alpha 3, or next major bugfix run
//    @Override
//    public void onPlayerDestroy(IWorld world, BlockPos pos, BlockState state) {
//
//        if (world.isRemote())
//            return;
//
//        Direction f = state.get(FACING);
//        ProjectorHelper.getClosestOppositeSize(world, pos, f)
//                .ifPresent(fieldSize -> {
//                    Optional<BlockPos> centerForSize = ProjectorHelper.getCenterForSize(pos, f,  fieldSize);
//                    centerForSize.ifPresent(center -> {
//                        BlockPos mainProjectorPosition = ProjectorHelper.getProjectorLocationForDirection(center, Direction.NORTH, fieldSize);
//
//                        FieldProjectorTile mainProjectorTE = (FieldProjectorTile) world.getTileEntity(mainProjectorPosition);
//                        if (mainProjectorTE == null)
//                            return;
//
//                        mainProjectorTE.invalidateField();
//                    });
//                });
//    }
}
