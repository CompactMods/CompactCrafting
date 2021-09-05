package dev.compactmods.crafting.proxies.block;

import javax.annotation.Nullable;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class MatchFieldProxyBlock extends FieldProxyBlock {

    public MatchFieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MatchFieldProxyEntity();
    }

    @Override
    public void onPlace(BlockState currState, World level, BlockPos placedAt, BlockState prevState, boolean update) {
        super.onPlace(currState, level, placedAt, prevState, update);

        MatchFieldProxyEntity tile = (MatchFieldProxyEntity) level.getBlockEntity(placedAt);
        if (tile != null) {
            tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                    .ifPresent(field -> {
                        int signal = field.getCurrentRecipe().isPresent() ? 15 : 0;
                        level.setBlock(placedAt, currState.setValue(SIGNAL, signal), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                    });
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        return state.getValue(SIGNAL);
    }
}
