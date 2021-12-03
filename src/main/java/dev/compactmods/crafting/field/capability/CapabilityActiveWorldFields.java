package dev.compactmods.crafting.field.capability;

import dev.compactmods.crafting.api.field.IActiveWorldFields;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityActiveWorldFields {

    public static Capability<IActiveWorldFields> FIELDS = CapabilityManager.get(new CapabilityToken<IActiveWorldFields>() {
    });
}
