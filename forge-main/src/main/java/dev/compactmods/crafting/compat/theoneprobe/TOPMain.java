package dev.compactmods.crafting.compat.theoneprobe;

import dev.compactmods.crafting.compat.theoneprobe.providers.FieldProjectorProvider;
import dev.compactmods.crafting.compat.theoneprobe.providers.FieldProxyProvider;
import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

class TOPMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        if (o instanceof ITheOneProbe probe) {
            PROBE = probe;
            probe.registerProvider(new FieldProjectorProvider());
            probe.registerProvider(new FieldProxyProvider());
        }

        return null;
    }
}
