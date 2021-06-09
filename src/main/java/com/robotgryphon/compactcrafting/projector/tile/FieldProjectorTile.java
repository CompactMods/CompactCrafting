package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public abstract class FieldProjectorTile extends TileEntity {

    public FieldProjectorTile() {
        super(Registration.MAIN_FIELD_PROJECTOR_TILE.get());
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

    public Optional<MiniaturizationField> getField() {
        return getMainProjectorTile().flatMap(MainFieldProjectorTile::getField);
    }
}
