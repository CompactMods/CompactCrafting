package dev.compactmods.crafting.proxies;

import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.proxies.listener.ConstantAfterMatchFieldListener;
import dev.compactmods.crafting.proxies.listener.ConstantWhileCraftingFieldListener;
import dev.compactmods.crafting.proxies.listener.PulseOnMatchFieldListener;
import dev.compactmods.crafting.proxies.listener.PulseOnStartedFieldListener;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Supplier;

public enum MatchProxyMode implements StringRepresentable {
    PULSE(true, PulseOnMatchFieldListener::new),
    PULSE_STARTED(true, PulseOnStartedFieldListener::new),
    CONSTANT(false, ConstantAfterMatchFieldListener::new),
    CONSTANT_CRAFTING(false, ConstantWhileCraftingFieldListener::new);

    private final boolean isPulse;
    private final Supplier<IFieldListener> supplier;

    MatchProxyMode(boolean isPulse, Supplier<IFieldListener> listener) {
        this.isPulse = isPulse;
        this.supplier = listener;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @NotNull
    public Supplier<IFieldListener> supplier() {
        return this.supplier;
    }
}
