package com.robotgryphon.compactcrafting.proxies.block;

import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.proxies.data.FieldProxyTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FieldProxyBlock extends Block {
    public FieldProxyBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FieldProxyTile();
    }

    @Override
    public void setPlacedBy(World level, BlockPos placedAt, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, placedAt, state, entity, stack);

        FieldProxyTile tile = (FieldProxyTile) level.getBlockEntity(placedAt);

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
    public void neighborChanged(BlockState thisState, World level, BlockPos thisPos, Block changedBlock, BlockPos changedPos, boolean _b) {
        if(!level.isClientSide) {
            FieldProxyTile tile = (FieldProxyTile) level.getBlockEntity(thisPos);

            if(level.hasNeighborSignal(thisPos)) {
                // call recipe scan

                if(tile != null) {
                    tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                            .ifPresent(field -> field.markFieldChanged(level));
                }
            }
        }
    }
}
