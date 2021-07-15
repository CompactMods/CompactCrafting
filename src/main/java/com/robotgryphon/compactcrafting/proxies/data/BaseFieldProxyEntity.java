package com.robotgryphon.compactcrafting.proxies.data;

import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseFieldProxyEntity extends TileEntity {

    @Nullable
    protected BlockPos fieldCenter;

    protected LazyOptional<IMiniaturizationField> field = LazyOptional.empty();

    public BaseFieldProxyEntity(TileEntityType<? extends BaseFieldProxyEntity> type) {
        super(type);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if(fieldCenter != null && level != null) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .resolve()
                    .ifPresent(fields -> fieldChanged(fields.getLazy(fieldCenter)));
        }
    }

    public void updateField(BlockPos fieldCenter) {
        if(level == null)
            return;

        if(fieldCenter == null) {
            this.field = LazyOptional.empty();
            this.fieldCenter = null;
            return;
        }

        level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .resolve()
                .map(fields -> fields.getLazy(fieldCenter))
                .ifPresent(f -> {
                    this.fieldCenter = fieldCenter;

                    fieldChanged(f);
                });
    }

    protected void fieldChanged(LazyOptional<IMiniaturizationField> f) {
        this.field = f;

        // field invalidated somewhere
        f.addListener(lof -> {
            this.field = LazyOptional.empty();
            this.fieldCenter = null;
        });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return field.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        tag = super.save(tag);

        CompoundNBT finalTag = tag;
        field.resolve().ifPresent(field -> {
            finalTag.put("center", NBTUtil.writeBlockPos(field.getCenter()));
        });

        return finalTag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        if(tag.contains("center")) {
            this.fieldCenter = NBTUtil.readBlockPos(tag.getCompound("center"));
        }
    }
}
