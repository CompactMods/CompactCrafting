package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.render.GhostProjectorPlacementRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FieldProjectorBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final EnumProperty<MiniaturizationFieldSize> SIZE = EnumProperty.create("field", MiniaturizationFieldSize.class);

    private static final VoxelShape BASE = Shapes.box(0, 0, 0, 1, 6 / 16d, 1);

    private static final VoxelShape POLE = Shapes.box(7 / 16d, 6 / 16d, 7 / 16d, 9 / 16d, 12 / 16d, 9 / 16d);

    private static final VoxelShape DISH_WEST = Shapes.box(3 / 16d, 0.5d, 3 / 16d,
            7 / 16d, 1, 13 / 16d);

    private static final VoxelShape DISH_EAST = Shapes.box(9 / 16d, 0.5d, 3 / 16d,
            13 / 16d, 1, 13 / 16d);

    private static final VoxelShape DISH_NORTH = Shapes.box(3 / 16d, 0.5d, 3 / 16d,
            13 / 16d, 1, 7 / 16d);

    private static final VoxelShape DISH_SOUTH = Shapes.box(3 / 16d, 0.5d, 9 / 16d,
            13 / 16d, 1, 13 / 16d);

    public FieldProjectorBlock(Properties properties) {
        super(properties);

        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SIZE, MiniaturizationFieldSize.INACTIVE));
    }

    public static Optional<Direction> getDirection(BlockGetter world, BlockPos position) {
        BlockState positionState = world.getBlockState(position);

        // The passed block position was not a field projector, cannot continue
        if (!(positionState.getBlock() instanceof FieldProjectorBlock))
            return Optional.empty();

        Direction facing = positionState.getValue(FieldProjectorBlock.FACING);
        return Optional.of(facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter levelReader, BlockPos pos, CollisionContext ctx) {
        Direction dir = state.getValue(FieldProjectorBlock.FACING);

        switch (dir) {
            case WEST:
                return Shapes.or(BASE, POLE, DISH_WEST);

            case NORTH:
                return Shapes.or(BASE, POLE, DISH_NORTH);

            case EAST:
                return Shapes.or(BASE, POLE, DISH_EAST);

            case SOUTH:
                return Shapes.or(BASE, POLE, DISH_SOUTH);
        }

        return Shapes.or(BASE, POLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(SIZE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction looking = context.getHorizontalDirection();

        // Hold shift to make the projector face you; else face away
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            looking = looking.getOpposite();

        Stream<BlockPos> missing = ProjectorHelper.getMissingProjectors(level, pos, looking);
        BlockPos[] missingSpots = missing.toArray(BlockPos[]::new);
        boolean hasMissing = Arrays.stream(missingSpots).anyMatch(p -> !p.equals(pos));

        BlockState state = defaultBlockState().setValue(FACING, looking);
        if (!hasMissing) {
            MiniaturizationFieldSize size = ProjectorHelper.getClosestOppositeSize(level, pos, looking)
                    .orElse(MiniaturizationFieldSize.INACTIVE);

            state = state.setValue(SIZE, size);
        } else {
            state = state.setValue(SIZE, MiniaturizationFieldSize.INACTIVE);
        }

        return state;
    }

    public static boolean isActive(BlockState state) {
        return state.getValue(SIZE) != MiniaturizationFieldSize.INACTIVE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (world.isClientSide) {
            final boolean hasMissing = ProjectorHelper.getMissingProjectors(world, pos, state.getValue(FACING)).findAny().isPresent();
            if (hasMissing) {
                GhostProjectorPlacementRenderer.resetRenderTime();
                GhostProjectorPlacementRenderer.setOriginProjector(world, pos);
            }

            return InteractionResult.SUCCESS;
        }

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

        return InteractionResult.SUCCESS;
    }

    public static BlockPos getFieldCenter(BlockState state, BlockPos projector) {
        return state.getValue(SIZE).getCenterFromProjector(projector, state.getValue(FACING));
    }

    public static void deactivateProjector(Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() instanceof FieldProjectorBlock) {
            BlockState newState = currentState.setValue(SIZE, MiniaturizationFieldSize.INACTIVE);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.removeBlockEntity(pos);
        }
    }

    public static void activateProjector(Level level, BlockPos pos, MiniaturizationFieldSize fieldSize) {
        if (level.isLoaded(pos)) {
            BlockState currentState = level.getBlockState(pos);
            if (!(currentState.getBlock() instanceof FieldProjectorBlock)) {
                return;
            }

            if (currentState.getValue(SIZE) != fieldSize) {
                BlockState newState = currentState.setValue(SIZE, fieldSize);
                level.setBlock(pos, newState, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean b) {
        if (!isActive(state))
            return;

        MiniaturizationFieldSize fieldSize = state.getValue(SIZE);
        BlockPos fieldCenter = fieldSize.getCenterFromProjector(pos, state.getValue(FACING));

        boolean hasMissing = ProjectorHelper.getMissingProjectors(level, pos, state.getValue(FACING))
                .findAny().isPresent();

        // If there are missing projectors but the projector is supposed to be active, deactivate
        if (hasMissing) {
            level.setBlock(pos, state.setValue(SIZE, MiniaturizationFieldSize.INACTIVE), Block.UPDATE_ALL);
        } else {
            final MinecraftServer server = level.getServer();
            if (server == null)
                return;

            if (level.isAreaLoaded(fieldCenter, fieldSize.getProjectorDistance())) {
                fieldSize.getProjectorLocations(fieldCenter).forEach(proj -> activateProjector(level, proj, fieldSize));

                final BlockPos center = getFieldCenter(state, pos);

//                level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
//                    if (!fields.hasActiveField(center)) {
//                        final IMiniaturizationField field = fields.registerField(MiniaturizationField.fromSizeAndCenter(fieldSize, center));
//                        field.checkLoaded();
//                        field.fieldContentsChanged();
//
////                            field.getProjectorPositions()
////                                    .map(level::getBlockEntity)
////                                    .filter(tile -> tile instanceof FieldProjectorTile)
////                                    .map(tile -> (FieldProjectorTile) tile)
////                                    .forEach(tile -> tile.setFieldRef(field.getRef()));
//
//                        // Send activation packet to clients
//                        NetworkHandler.MAIN_CHANNEL.send(
//                                PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(field.getCenter())),
//                                new FieldActivatedPacket(field));
//                    }
//                });
            }
        }
    }

    // only called on the server
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean p_196243_5_) {
        final BlockPos fieldCenter = getFieldCenter(oldState, pos);
        final MiniaturizationFieldSize fieldSize = oldState.getValue(SIZE);

        if (isActive(oldState)) {
            fieldSize.getProjectorLocations(fieldCenter).forEach(proj -> deactivateProjector(level, proj));

            // Remove field registration - this will also update clients
            // FIXME AAAAAAAAAAAAAAAAAAAA
//            level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
//                if (fields.hasActiveField(fieldCenter)) {
//                    final IMiniaturizationField field = fields.get(fieldCenter).orElse(null);
//                    if (field == null) return;
//
//                    if (field.enabled()) {
//                        fields.unregisterField(fieldCenter);
//                        field.handleDestabilize();
//                        field.dispose();
//                    }
//                }
//            });
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changer, BlockPos changedPos, boolean update) {
        super.neighborChanged(state, level, pos, changer, changedPos, update);
        if (level.isClientSide)
            return;

        // FIXME REDSTONE HANDLING
//        if (isActive(state)) {
//            BlockEntity tile = level.getBlockEntity(pos);
//            if (tile instanceof FieldProjectorEntity) {
//                FieldProjectorEntity fpt = (FieldProjectorEntity) tile;
//                if (level.getBestNeighborSignal(pos) > 0) {
//                    // receiving power from some side, turn off rendering
//                    fpt.getField().ifPresent(IMiniaturizationField::disable);
//                } else {
//                    // check other projectors, if there's a redstone signal anywhere, we disable the field
//                    fpt.getField().ifPresent(IMiniaturizationField::checkRedstone);
//                }
//            }
//        } else {
//            // not active, but we may be re-enabling a disabled field
//            ProjectorHelper.getClosestOppositeSize(level, pos).ifPresent(size -> {
//                final BlockPos center = size.getCenterFromProjector(pos, state.getValue(FACING));
//                level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
//                    fields.get(center).ifPresent(IMiniaturizationField::checkRedstone);
//                });
//            });
//        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isActive(state))
            return new FieldProjectorEntity(pos, state);

        return null;
    }
}
