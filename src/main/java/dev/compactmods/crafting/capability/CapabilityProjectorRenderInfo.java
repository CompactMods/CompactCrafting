package dev.compactmods.crafting.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityProjectorRenderInfo {

    public static Capability<IProjectorRenderInfo> TEMP_PROJECTOR_RENDERING = CapabilityManager.get(new CapabilityToken<>() {});

}
