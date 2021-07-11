package com.robotgryphon.compactcrafting.proxies.block;

import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.proxies.ProxyMode;
import com.robotgryphon.compactcrafting.proxies.data.BaseFieldProxyEntity;
import com.robotgryphon.compactcrafting.proxies.data.MatchFieldProxyEntity;
import com.robotgryphon.compactcrafting.proxies.data.RescanFieldProxyEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FieldProxyBlock extends Block {
    private final ProxyMode mode;

    public static IntegerProperty SIGNAL = BlockStateProperties.POWER;

    public FieldProxyBlock(ProxyMode mode, Properties props) {
        super(props);
        this.mode = mode;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(SIGNAL);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        switch(mode) {
            case RESCAN:
                return new RescanFieldProxyEntity();

            case MATCH:
                return new MatchFieldProxyEntity();
        }

        return null;
    }

    @Override
    public void setPlacedBy(World level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(placedAt);

        if(stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();

            if(nbt.contains("field")) {
                CompoundNBT fieldData = nbt.getCompound("field");
                if(fieldData.contains("center")) {
                    BlockPos center = NBTUtil.readBlockPos(fieldData.getCompound("center"));

                    if(tile != null)
                        tile.updateField(center);
                }
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        switch(mode) {
            case MATCH:
            case PROGRESS:
                return true;

            case RESCAN:
                return false;
        }

        return false;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader level, BlockPos pos, Direction direction) {
        switch(mode) {
            case RESCAN:
                return 0;

            case MATCH:
            case PROGRESS:
                return state.getValue(SIGNAL);
        }

        return 0;
    }

    @Override
    public void neighborChanged(BlockState thisState, World level, BlockPos thisPos, Block changedBlock, BlockPos changedPos, boolean _b) {
        if(!level.isClientSide) {
            BaseFieldProxyEntity tile = (BaseFieldProxyEntity) level.getBlockEntity(thisPos);

            if (mode == ProxyMode.RESCAN) {
                doRescanRedstone(level, thisPos, tile);
            }
        }
    }

    private void doRescanRedstone(World level, BlockPos thisPos, BaseFieldProxyEntity tile) {
        if (level.hasNeighborSignal(thisPos)) {
            // call recipe scan

            if (tile != null) {
                tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                        .ifPresent(IMiniaturizationField::markFieldChanged);
            }
        }
    }
}
