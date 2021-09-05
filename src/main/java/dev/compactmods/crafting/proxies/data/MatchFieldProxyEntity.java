package dev.compactmods.crafting.proxies.data;

import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.field.capability.CapabilityFieldListener;
import dev.compactmods.crafting.proxies.listener.MatchModeProxyFieldListener;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
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
