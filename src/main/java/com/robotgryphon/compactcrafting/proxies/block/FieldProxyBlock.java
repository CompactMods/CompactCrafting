package com.robotgryphon.compactcrafting.proxies.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
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
        return null;
    }

    @Override
    public void onPlace(BlockState state, World level, BlockPos placedAt, BlockState oldState, boolean notify) {
        super.onPlace(state, level, placedAt, oldState, notify);
    }
}
