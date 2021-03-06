package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class DummyFieldProjectorTile extends FieldProjectorTile {
    private BlockPos mainProjector;
    private BlockPos fieldCenter;

    private void setMainProjector(BlockPos main) {
        this.mainProjector = main;
    }

    @Override
    public Optional<BlockPos> getMainProjectorPosition() {
        return Optional.ofNullable(mainProjector);
    }

    @Override
    public Optional<MainFieldProjectorTile> getMainProjectorTile() {
        if(world == null || mainProjector == null)
            return Optional.empty();

        TileEntity tile = world.getTileEntity(mainProjector);
        if(tile instanceof MainFieldProjectorTile)
            return Optional.of((MainFieldProjectorTile) tile);
        else
            return Optional.empty();
    }

    public void onInitialPlacement() {
        // Need to scan for main projector and update accordingly
        Direction facing = getFacing();
        Optional<FieldProjectionSize> size = ProjectorHelper.getClosestOppositeSize(world, pos, facing);
        if (!size.isPresent()) {
            this.fieldCenter = null;
            this.mainProjector = null;
            return;
        }

        FieldProjectionSize fieldSize = size.get();
        Optional<BlockPos> center = ProjectorHelper.getCenterForSize(pos, facing, fieldSize);
        if (!center.isPresent())
            return;

        this.fieldCenter = center.get();
        this.mainProjector = ProjectorHelper.getProjectorLocationForDirection(fieldCenter, Direction.NORTH, fieldSize);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt = super.write(compound);

        if(mainProjector != null)
            nbt.put("main", NBTUtil.writeBlockPos(mainProjector));

        if(fieldCenter != null)
            nbt.put("center", NBTUtil.writeBlockPos(fieldCenter));

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        if(nbt.contains("main"))
            mainProjector = NBTUtil.readBlockPos(nbt.getCompound("main"));

        if(nbt.contains("center"))
            fieldCenter = NBTUtil.readBlockPos(nbt.getCompound("center"));
    }
}
