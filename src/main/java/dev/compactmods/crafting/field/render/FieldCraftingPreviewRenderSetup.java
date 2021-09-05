package dev.compactmods.crafting.field.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FieldCraftingPreviewRenderSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(Registration.FIELD_CRAFTING_PREVIEW_TILE.get(), FieldCraftingPreviewRenderer::new);
    }

}
