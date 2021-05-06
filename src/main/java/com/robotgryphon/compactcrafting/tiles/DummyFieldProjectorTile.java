package com.robotgryphon.compactcrafting.tiles;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public class DummyFieldProjectorTile extends FieldProjectorTile {
    private BlockPos mainProjector;
    private BlockPos fieldCenter;

    public DummyFieldProjectorTile() {
        super(Registration.DUMMY_FIELD_PROJECTOR_TILE.get());
    }

    @Override
    public Optional<BlockPos> getMainProjectorPosition() {
        return Optional.ofNullable(this.mainProjector);
    }

    @Override
    public Optional<MainFieldProjectorTile> getMainProjectorTile() {
        if(level == null || mainProjector == null)
            return Optional.empty();

        TileEntity tile = level.getBlockEntity(mainProjector);
        if(tile instanceof MainFieldProjectorTile)
            return Optional.of((MainFieldProjectorTile) tile);
        else
            return Optional.empty();
    }

    public void onInitialPlacement() {
        // Need to scan for main projector and update accordingly
        Direction facing = getFacing();
        Optional<FieldProjectionSize> size = ProjectorHelper.getClosestOppositeSize(level, worldPosition, facing);
        if (!size.isPresent()) {
            this.fieldCenter = null;
            this.mainProjector = null;
            return;
        }

        FieldProjectionSize fieldSize = size.get();
        Optional<BlockPos> center = ProjectorHelper.getCenterForSize(worldPosition, facing, fieldSize);
        if (!center.isPresent())
            return;

        this.fieldCenter = center.get();
        this.mainProjector = ProjectorHelper.getProjectorLocationForDirection(fieldCenter, Direction.NORTH, fieldSize);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        CompoundNBT nbt = super.save(compound);

        if (mainProjector != null)
            nbt.put("main", NBTUtil.writeBlockPos(mainProjector));

        if (fieldCenter != null)
            nbt.put("center", NBTUtil.writeBlockPos(fieldCenter));

        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);

        if (nbt.contains("main", Constants.NBT.TAG_COMPOUND))
            mainProjector = NBTUtil.readBlockPos(nbt.getCompound("main"));

        if (nbt.contains("center", Constants.NBT.TAG_COMPOUND))
            fieldCenter = NBTUtil.readBlockPos(nbt.getCompound("center"));
    }
}
