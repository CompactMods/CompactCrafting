package dev.compactmods.crafting.proxies.block;

import javax.annotation.Nullable;

import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.proxies.MatchProxyMode;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Random;

public class MatchFieldProxyBlock extends FieldProxyBlock implements EntityBlock {

    public static EnumProperty<MatchProxyMode> MATCH_MODE = EnumProperty.create("mode", MatchProxyMode.class);

    public MatchFieldProxyBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(MATCH_MODE, MatchProxyMode.CONSTANT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MATCH_MODE);
    }

    @Override
    public void onPlace(BlockState currState, Level level, BlockPos placedAt, BlockState prevState, boolean update) {
        super.onPlace(currState, level, placedAt, prevState, update);

        MatchFieldProxyEntity tile = (MatchFieldProxyEntity) level.getBlockEntity(placedAt);
        if (tile != null) {
            tile.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                    .ifPresent(field -> {
                        int signal = field.getCurrentRecipe().isPresent() ? 15 : 0;
                        level.setBlock(placedAt, currState.setValue(SIGNAL, signal), Block.UPDATE_ALL);
                    });
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(SIGNAL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MatchFieldProxyEntity(pos, state);
    }

    @Override
    public void tick(BlockState pState, ServerLevel level, BlockPos pos, Random rand) {
        // Similar to redstone repeaters, a scheduled tick turns off a pulsing field proxy
        final var mode = pState.getValue(MATCH_MODE);
        if(mode.isPulse) {
            var update = pState.setValue(SIGNAL, 0);
            level.setBlock(pos, update, Block.UPDATE_ALL);
        }
    }
}
