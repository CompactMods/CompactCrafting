package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.render.FieldCraftingPreviewRenderer;
import com.robotgryphon.compactcrafting.client.render.FieldProjectorRenderer;
import com.robotgryphon.compactcrafting.core.Constants;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.ui.container.ContainerRegistration;
import com.robotgryphon.compactcrafting.ui.gui.TestScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.register(
                ContainerRegistration.TEST_CONTAINER.get(),
                TestScreen::new);

        ClientRegistry.bindTileEntityRenderer(Registration.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Registration.FIELD_CRAFTING_PREVIEW_TILE.get(), FieldCraftingPreviewRenderer::new);
    }

    @SubscribeEvent
    public static void registerSpecialModels(final ModelRegistryEvent registryEvent) {
        ModelLoader.addSpecialModel(Constants.FIELD_DISH_RL);
    }
}
