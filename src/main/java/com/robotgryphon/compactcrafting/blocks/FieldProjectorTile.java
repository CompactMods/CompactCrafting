package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.config.ClientConfig;
import com.robotgryphon.compactcrafting.core.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.Optional;

public abstract class FieldProjectorTile extends TileEntity {

    public FieldProjectorTile() {
        super(Registration.FIELD_PROJECTOR_TILE.get());
    }

    public Color getProjectionColor(EnumProjectorColorType type) {
        Color base = ClientConfig.projectorColor;
        new Color(255, 106, 0, 100);
        // Color base = Color.red.brighter();
        int red = base.getRed();
        int green = base.getGreen();
        int blue = base.getBlue();

        switch (type) {
            case FIELD:
            case SCAN_LINE:
                return new Color(red, green, blue, 100);

            case PROJECTOR_FACE:
                // return new Color(Color.cyan.getRed(), Color.cyan.getGreen(), Color.cyan.getBlue(), 100);
                return new Color(red, green, blue, 250);
        }

        return Color.WHITE;
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        return bs.get(FieldProjectorBlock.FACING);
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

    public Optional<FieldProjection> getField() {
        return getMainProjectorTile().flatMap(MainFieldProjectorTile::getField);
    }
}
