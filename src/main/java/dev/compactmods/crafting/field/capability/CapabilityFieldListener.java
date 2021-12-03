package dev.compactmods.crafting.field.capability;

import dev.compactmods.crafting.api.field.IFieldListener;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityFieldListener {

    public static Capability<IFieldListener> FIELD_LISTENER = CapabilityManager.get(new CapabilityToken<IFieldListener>() {
    });
}
