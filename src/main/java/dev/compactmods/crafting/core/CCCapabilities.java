package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.MiniaturizationField;
import dev.compactmods.crafting.api.projector.IProjectorRenderInfo;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class CCCapabilities {

    public static Capability<IProjectorRenderInfo> TEMP_PROJECTOR_RENDERING = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Capability<IActiveWorldFields> FIELDS = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Capability<IFieldListener> FIELD_LISTENER = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Capability<MiniaturizationField> MINIATURIZATION_FIELD = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent evt) {
        evt.register(IProjectorRenderInfo.class);
        evt.register(MiniaturizationField.class);
        evt.register(IActiveWorldFields.class);
        evt.register(IFieldListener.class);
    }
}
