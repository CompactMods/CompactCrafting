package com.robotgryphon.compactcrafting.projector.block;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.network.FieldActivatedPacket;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.projector.ProjectorHelper;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FieldProjectorBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final EnumProperty<FieldProjectionSize> SIZE = EnumProperty.create("field", FieldProjectionSize.class);

    private static final VoxelShape BASE = VoxelShapes.box(0, 0, 0, 1, 6 / 16d, 1);

    private static final VoxelShape POLE = VoxelShapes.box(7 / 16d, 6 / 16d, 7 / 16d, 9 / 16d, 12 / 16d, 9 / 16d);

    private static final VoxelShape DISH_WEST = VoxelShapes.box(3/16d, 0.5d, 3/16d,
            7/16d, 1, 13/16d);

    private static final VoxelShape DISH_EAST = VoxelShapes.box(9/16d, 0.5d, 3/16d,
            13/16d, 1, 13/16d);

    private static final VoxelShape DISH_NORTH = VoxelShapes.box(3/16d, 0.5d, 3/16d,
            13/16d, 1, 7/16d);

    private static final VoxelShape DISH_SOUTH = VoxelShapes.box(3/16d, 0.5d, 9/16d,
            13/16d, 1, 13/16d);

    public FieldProjectorBlock(Properties properties) {
        super(properties);

        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SIZE, FieldProjectionSize.INACTIVE));
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
    public VoxelShape getShape(BlockState state, IBlockReader levelReader, BlockPos pos, ISelectionContext ctx) {
        Direction dir = state.getValue(FieldProjectorBlock.FACING);

        switch(dir) {
            case WEST:
                return VoxelShapes.or(BASE, POLE, DISH_WEST);

            case NORTH:
                return VoxelShapes.or(BASE, POLE, DISH_NORTH);

            case EAST:
                return VoxelShapes.or(BASE, POLE, DISH_EAST);

            case SOUTH:
                return VoxelShapes.or(BASE, POLE, DISH_SOUTH);
        }

        return VoxelShapes.or(BASE, POLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(SIZE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction looking = context.getHorizontalDirection();

        // Hold shift to make the projector face you; else face away
        if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            looking = looking.getOpposite();

        Stream<BlockPos> missing = ProjectorHelper.getMissingProjectors(level, pos, looking);
        BlockPos[] missingSpots = missing.toArray(BlockPos[]::new);
        boolean hasMissing = Arrays.stream(missingSpots).anyMatch(p -> !p.equals(pos));

        BlockState state = defaultBlockState().setValue(FACING, looking);
        if(!hasMissing) {
            FieldProjectionSize size = ProjectorHelper.getClosestOppositeSize(level, pos, looking)
                    .orElse(FieldProjectionSize.INACTIVE);

            state = state.setValue(SIZE, size);
        } else {
            state = state.setValue(SIZE, FieldProjectionSize.INACTIVE);
        }

        return state;
    }

    public static boolean isActive(BlockState state) {
        return state.getValue(SIZE) != FieldProjectionSize.INACTIVE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return isActive(state);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if(isActive(state))
            return new FieldProjectorTile(state.getValue(SIZE));

        return null;
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

    public static void deactivateProjector(World level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        level.setBlock(pos, currentState.setValue(SIZE, FieldProjectionSize.INACTIVE), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    public static void activateProjector(World level, BlockPos pos, FieldProjectionSize fieldSize) {
        BlockState currentState = level.getBlockState(pos);
        if(currentState.getValue(SIZE) != fieldSize)
            level.setBlock(pos, currentState.setValue(SIZE, fieldSize), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Override
    public void onPlace(BlockState state, World level, BlockPos pos, BlockState oldState, boolean b) {
        if(isActive(state) && !level.isClientSide) {
            FieldProjectionSize fieldSize = state.getValue(SIZE);
            BlockPos fieldCenter = fieldSize.getCenterFromProjector(pos, state.getValue(FACING));

            fieldSize.getProjectorLocations(fieldCenter)
                    .forEach(proj -> activateProjector(level, proj, fieldSize));

            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .ifPresent(fields -> {
                        fields.get(fieldCenter).ifPresent(IMiniaturizationField::checkLoaded);
                    });

            // Send activation packet to clients
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                    new FieldActivatedPacket(fieldSize, fieldCenter));
        }
    }
}
