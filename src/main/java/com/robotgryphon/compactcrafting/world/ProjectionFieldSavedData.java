package com.robotgryphon.compactcrafting.world;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class ProjectionFieldSavedData extends WorldSavedData {
    public static final String DATA_NAME = CompactCrafting.MOD_ID + "_projectionfields";
    private final Map<BlockPos, ProjectorFieldData> activeFields = new HashMap<>();

    public ProjectionFieldSavedData() {
        super(DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        ListNBT fields = nbt.getList("fields", Constants.NBT.TAG_COMPOUND);
        for(INBT fieldTag : fields) {
            ProjectorFieldData fd = ProjectorFieldData.deserialize(fieldTag);
            if(fd != null)
                this.activeFields.put(fd.fieldCenter, fd);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for(ProjectorFieldData pf : this.activeFields.values()) {
            CompoundNBT data = pf.serialize();
            list.add(data);
        }

        compound.put("fields", list);
        return compound;
    }

    public static ProjectionFieldSavedData get(ServerWorld world) {
        DimensionSavedDataManager wsd = world.getDataStorage();
        return wsd.computeIfAbsent(ProjectionFieldSavedData::new, DATA_NAME);
    }

    public void unregister(BlockPos center) {
        this.activeFields.remove(center);
        this.setDirty();
    }

    public Map<BlockPos, ProjectorFieldData> getActiveFields() {
        return this.activeFields;
    }
}
