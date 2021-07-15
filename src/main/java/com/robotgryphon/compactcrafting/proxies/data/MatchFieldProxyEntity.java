package com.robotgryphon.compactcrafting.proxies.data;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.proxies.listener.MatchModeProxyFieldListener;
import dev.compactmods.compactcrafting.api.field.IFieldListener;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraftforge.common.util.LazyOptional;

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

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();

        listener.invalidate();
    }
}
