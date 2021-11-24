package dev.compactmods.crafting.capability;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityProjectorRenderInfo {
    @CapabilityInject(IProjectorRenderInfo.class)
    public static Capability<IProjectorRenderInfo> TEMP_PROJECTOR_RENDERING = null;

    public static void setup() {
        CapabilityManager.INSTANCE.register(
                IProjectorRenderInfo.class,
                new Capability.IStorage<IProjectorRenderInfo>() {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<IProjectorRenderInfo> capability, IProjectorRenderInfo instance, Direction side) {
                        return new CompoundNBT();
                    }

                    @Override
                    public void readNBT(Capability<IProjectorRenderInfo> capability, IProjectorRenderInfo instance, Direction side, INBT nbt) {

                    }
                }, null);
    }
}
