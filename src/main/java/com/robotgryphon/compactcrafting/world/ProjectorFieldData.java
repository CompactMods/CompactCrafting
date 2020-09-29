package com.robotgryphon.compactcrafting.world;

import com.robotgryphon.compactcrafting.field.FieldProjection;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class ProjectorFieldData {

    public BlockPos mainProjector;
    public BlockPos fieldCenter;

    public ProjectorFieldData(BlockPos mainProjector, BlockPos fieldCenter) {
        this.mainProjector = mainProjector;
        this.fieldCenter = fieldCenter;
    }

    public static ProjectorFieldData fromInstance(FieldProjection pf) {
        BlockPos center = pf.getCenterPosition();
        BlockPos main = pf.getProjectorInDirection(Direction.NORTH);

        ProjectorFieldData fd = new ProjectorFieldData(main, center);
        return fd;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        CompoundNBT mainPos = NBTUtil.writeBlockPos(mainProjector);
        CompoundNBT centerPos = NBTUtil.writeBlockPos(fieldCenter);

        tag.put("main", mainPos);
        tag.put("field", centerPos);

        return tag;
    }

    public static ProjectorFieldData deserialize(INBT fieldTag) {
        if(fieldTag instanceof CompoundNBT) {
            CompoundNBT fieldTagComp = (CompoundNBT) fieldTag;


            CompoundNBT mainProjectorComp = fieldTagComp.getCompound("main");
            BlockPos mainProjector = NBTUtil.readBlockPos(mainProjectorComp);

            CompoundNBT fieldCenterComp = fieldTagComp.getCompound("field");
            BlockPos fieldCenter = NBTUtil.readBlockPos(fieldTagComp);

            ProjectorFieldData fd = new ProjectorFieldData(mainProjector, fieldCenter);
            return fd;
        }

        return null;
    }
}
