package dev.compactmods.crafting.proxies.block;

import javax.annotation.Nullable;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MatchFieldProxyBlock extends FieldProxyBlock implements EntityBlock {

    public MatchFieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    public void onPlace(BlockState currState, Level level, BlockPos placedAt, BlockState prevState, boolean update) {
        super.onPlace(currState, level, placedAt, prevState, update);

        MatchFieldProxyEntity tile = (MatchFieldProxyEntity) level.getBlockEntity(placedAt);
        if (tile != null) {
            tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
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
}
