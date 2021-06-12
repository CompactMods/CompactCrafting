package com.robotgryphon.compactcrafting.field.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityMiniaturizationField {
    @CapabilityInject(IMiniaturizationField.class)
    public static Capability<IMiniaturizationField> MINIATURIZATION_FIELD = null;
}
