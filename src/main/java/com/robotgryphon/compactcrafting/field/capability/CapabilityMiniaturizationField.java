package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityMiniaturizationField {
    @CapabilityInject(IMiniaturizationField.class)
    public static Capability<IMiniaturizationField> MINIATURIZATION_FIELD = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IMiniaturizationField.class,
                new MiniaturizationFieldStorage(),
                MiniaturizationField::new);
    }
}
