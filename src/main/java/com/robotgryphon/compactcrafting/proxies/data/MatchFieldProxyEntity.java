package com.robotgryphon.compactcrafting.proxies.data;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.CapabilityFieldListener;
import com.robotgryphon.compactcrafting.proxies.listener.MatchModeProxyFieldListener;
import dev.compactmods.compactcrafting.api.field.IFieldListener;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MatchFieldProxyEntity extends BaseFieldProxyEntity {
    protected LazyOptional<IFieldListener> listener = LazyOptional.empty();

    public MatchFieldProxyEntity() {
        super(Registration.MATCH_PROXY_ENTITY.get());
    }

    @Override
    protected void fieldChanged(LazyOptional<IMiniaturizationField> f) {
        super.fieldChanged(f);

        MatchModeProxyFieldListener listener = new MatchModeProxyFieldListener(level, worldPosition);

        this.listener = LazyOptional.of(() -> listener);

        // if field actually present, register this proxy
        f.ifPresent(f2 -> f2.registerListener(this.listener));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityFieldListener.FIELD_LISTENER)
            return listener.cast();

        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();

        listener.invalidate();
    }
}
