package com.robotgryphon.compactcrafting.world;

import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class ProjectorFieldData {

    public BlockPos mainProjector;
    public BlockPos fieldCenter;
    public FieldProjectionSize size;

    private ProjectorFieldData(BlockPos mainProjector, BlockPos fieldCenter) {
        this.mainProjector = mainProjector;
        this.fieldCenter = fieldCenter;
    }

    private ProjectorFieldData(FieldProjectionSize fieldSize, BlockPos mainProjector, BlockPos fieldCenter) {
        this.mainProjector = mainProjector;
        this.fieldCenter = fieldCenter;
        this.size = fieldSize;
    }

    public static ProjectorFieldData fromInstance(FieldProjection pf) {
        BlockPos center = pf.getCenterPosition();
        BlockPos main = pf.getProjectorInDirection(Direction.NORTH);

        return new ProjectorFieldData(pf.getFieldSize(), main, center);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mainPos = NBTUtil.writeBlockPos(mainProjector);
        CompoundNBT centerPos = NBTUtil.writeBlockPos(fieldCenter);

        tag.put("main", mainPos);
        tag.put("field", centerPos);

        if(size != null)
            tag.putString("size", size.name());

        return tag;
    }

    public static ProjectorFieldData deserialize(INBT fieldTag) {
        if (fieldTag instanceof CompoundNBT) {
            CompoundNBT fieldTagComp = (CompoundNBT) fieldTag;


            CompoundNBT mainProjectorComp = fieldTagComp.getCompound("main");
            BlockPos mainProjector = NBTUtil.readBlockPos(mainProjectorComp);

            CompoundNBT fieldCenterComp = fieldTagComp.getCompound("field");
            BlockPos fieldCenter = NBTUtil.readBlockPos(fieldCenterComp);

            ProjectorFieldData fd = new ProjectorFieldData(mainProjector, fieldCenter);
            if (fieldTagComp.contains("size")) {
                String sSize = fieldTagComp.getString("size");
                fd.size = FieldProjectionSize.valueOf(sSize);
            } else {
                // If there is no size in data, calculate it - we have the main (N) projector and center
                Optional<FieldProjectionSize> sizeByMainProjector =
                        ProjectorHelper.findSizeByMainProjector(fieldCenter, mainProjector);

                sizeByMainProjector.ifPresent(s -> fd.size = s);
            }

            return fd;
        }

        return null;
    }
}
