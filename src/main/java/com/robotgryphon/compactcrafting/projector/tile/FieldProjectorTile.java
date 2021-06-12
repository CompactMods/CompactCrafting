package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public abstract class FieldProjectorTile extends TileEntity {

    public FieldProjectorTile(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        return bs.getValue(FieldProjectorBlock.FACING);
    }

    public Direction getProjectorSide() {
        Direction facing = getFacing();
        return facing.getOpposite();
    }

    public boolean isMainProjector() {
        Direction side = getProjectorSide();

        // We're the main projector if we're to the NORTH
        return side == Direction.NORTH;
    }

    public abstract Optional<BlockPos> getMainProjectorPosition();

    public abstract Optional<MainFieldProjectorTile> getMainProjectorTile();
}
