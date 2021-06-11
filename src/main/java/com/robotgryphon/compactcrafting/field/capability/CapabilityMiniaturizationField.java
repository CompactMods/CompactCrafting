package com.robotgryphon.compactcrafting.field.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityMiniaturizationField {
    @CapabilityInject(IMiniaturizationFieldProvider.class)
    public static Capability<IMiniaturizationFieldProvider> MINIATURIZATION_FIELD = null;
}
