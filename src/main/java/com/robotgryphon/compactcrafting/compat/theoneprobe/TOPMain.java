package com.robotgryphon.compactcrafting.compat.theoneprobe;

import com.robotgryphon.compactcrafting.compat.theoneprobe.providers.FieldProjectorProvider;
import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

class TOPMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        PROBE = (ITheOneProbe) o;
        PROBE.registerProvider(new FieldProjectorProvider());

        return null;
    }
}
