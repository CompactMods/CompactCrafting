package com.robotgryphon.compactcrafting.field.capability;

import dev.compactmods.compactcrafting.api.field.IFieldListener;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityFieldListener {

    @CapabilityInject(IFieldListener.class)
    public static Capability<IFieldListener> FIELD_LISTENER = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IFieldListener.class,
                new Capability.IStorage<IFieldListener>() {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<IFieldListener> capability, IFieldListener instance, Direction side) {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IFieldListener> capability, IFieldListener instance, Direction side, INBT nbt) {

                    }
                }, () -> null);
    }
}
