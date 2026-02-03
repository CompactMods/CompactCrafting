package dev.compactmods.crafting.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.projector.ActiveProjectorInfo;
import dev.compactmods.crafting.api.projector.FieldProjectorProperties;
import dev.compactmods.crafting.api.projector.world.ActiveProjectorBlock;
import dev.compactmods.crafting.field.MiniaturizationFields;
import dev.compactmods.crafting.field.impl.MiniaturizationField;
import dev.compactmods.crafting.network.FieldActivatedPacket;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class ActiveFieldProjectorBlock extends FieldProjectorBlock implements ActiveProjectorBlock, EntityBlock {

    public ActiveFieldProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);

        registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(FieldProjectorProperties.SIZE, MiniaturizationFieldSize.SMALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FieldProjectorProperties.SIZE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        final var projectorInfo = ActiveProjectorInfo.from(level, GlobalPos.of(level.dimension(), pos)).orElseThrow();

        final MinecraftServer server = level.getServer();
        if (server == null)
            return;

        final var newField = projectorInfo.target();

        // Activate the other 3 projectors
        newField.projectors().enableAll(level);

        if (level instanceof ServerLevel sl) {
            final var fields = sl.getData(MiniaturizationFields.ACTIVE_FIELDS);
            if (!fields.hasActiveField(newField)) {
                // TODO - Separate client and server projectors classes
                final var field = new MiniaturizationField(sl, newField);
                fields.registerField(field);
                field.checkLoaded();
                field.fieldContentsChanged();

                // Send activation packet to clients
                PacketDistributor.sendToPlayersTrackingChunk(sl,
                        MathUtil.toChunkPosition(newField.center()),
                        new FieldActivatedPacket(newField));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FieldProjectorEntity(pos, state);
    }
}