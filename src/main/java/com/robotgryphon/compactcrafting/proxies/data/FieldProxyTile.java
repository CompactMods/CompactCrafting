package com.robotgryphon.compactcrafting.proxies.data;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FieldProxyTile extends TileEntity {

    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();

    public FieldProxyTile() {
        super(Registration.FIELD_PROXY_TILE.get());
    }

    public void updateField(BlockPos fieldCenter) {
        if(level == null)
            return;

        level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .resolve()
                .map(fields -> fields.getLazy(fieldCenter))
                .ifPresent(field -> {
                    this.field = field;

                    field.addListener(lof -> {
                        this.field = LazyOptional.empty();
                    });
                });
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
            return field.cast();

        return super.getCapability(cap, side);
    }
}
