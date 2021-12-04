package dev.compactmods.crafting.core;

import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CCCapabilities {

    public static Capability<IActiveWorldFields> FIELDS = CapabilityManager.get(new CapabilityToken<IActiveWorldFields>() {
    });

    public static Capability<IFieldListener> FIELD_LISTENER = CapabilityManager.get(new CapabilityToken<IFieldListener>() {
    });

    public static Capability<IMiniaturizationField> MINIATURIZATION_FIELD = CapabilityManager.get(new CapabilityToken<IMiniaturizationField>() {
    });
}
