package com.robotgryphon.compactcrafting.tiles;

import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.config.ClientConfig;
import com.robotgryphon.compactcrafting.core.EnumProjectorColorType;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.awt.Color;
import java.util.Optional;

public abstract class FieldProjectorTile extends TileEntity {
    protected FieldProjectorTile(TileEntityType<? extends FieldProjectorTile> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public Color getProjectionColor(EnumProjectorColorType type) {
        EnumCraftingState craftingState = getMainProjectorTile().map(MainFieldProjectorTile::getCraftingState).orElse(EnumCraftingState.NOT_MATCHED);
        Color color = craftingState == EnumCraftingState.DONE ? ClientConfig.getProjectorDoneColor() : ClientConfig.getProjectorColor();

        switch (type) {
            case FIELD:
            case SCAN_LINE:
                return cloneColorWithAlpha(color, 100);
            case PROJECTOR_FACE:
                // return new Color(Color.cyan.getRed(), Color.cyan.getGreen(), Color.cyan.getBlue(), 100);
                return cloneColorWithAlpha(color, 250);
        }

        return Color.WHITE;
    }

    private static Color cloneColorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Direction getFacing() {
        BlockState bs = this.getBlockState();
        return bs.getValue(FieldProjectorBlock.FACING);
    }

    public Direction getProjectorSide() {
        return getFacing().getOpposite();
    }

    public boolean isMainProjector() {
        // We're the main projector if we're to the NORTH
        return getProjectorSide() == Direction.NORTH;
    }

    public abstract Optional<BlockPos> getMainProjectorPosition();

    public abstract Optional<MainFieldProjectorTile> getMainProjectorTile();

    public Optional<FieldProjection> getField() {
        return getMainProjectorTile().flatMap(MainFieldProjectorTile::getField);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // Do field check
        this.getMainProjectorTile().ifPresent(MainFieldProjectorTile::invalidateField);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        load(getBlockState(), packet.getTag());
    }
}
