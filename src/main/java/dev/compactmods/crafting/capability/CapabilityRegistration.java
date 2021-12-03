package dev.compactmods.crafting.capability;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class CapabilityRegistration {

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent evt) {
        evt.register(IProjectorRenderInfo.class);
        evt.register(IMiniaturizationField.class);
        evt.register(IActiveWorldFields.class);
        evt.register(IFieldListener.class);
    }
}
