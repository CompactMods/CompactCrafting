package com.robotgryphon.compactcrafting.field.block;

import com.robotgryphon.compactcrafting.field.tile.FieldCraftingPreviewTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class FieldCraftingPreviewBlock extends Block {
    public FieldCraftingPreviewBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FieldCraftingPreviewTile();
    }
}
