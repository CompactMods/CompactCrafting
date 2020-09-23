package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.render.FieldProjectorRenderer;
import com.robotgryphon.compactcrafting.core.Constants;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        //ScreenManager.registerFactory(Registration.FIRSTBLOCK_CONTAINER.get(), FirstBlockScreen::new);
        // ModelLoaderRegistry.registerLoader(new ResourceLocation(MyTutorial.MODID, "fancyloader"), new FancyModelLoader());
        // MagicRenderer.register();

        // RenderTypeLookup.setRenderLayer(Registration.COMPLEX_MULTIPART.get(), RenderType.getTranslucent());
        // RenderTypeLookup.setRenderLayer(Registration.FANCYBLOCK.get(), (RenderType) -> true);

        ClientRegistry.bindTileEntityRenderer(Registration.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
    }

    public static void registerSpecialModels(final ModelRegistryEvent registryEvent) {
        ModelLoader.addSpecialModel(Constants.FIELD_DISH_RL);
    }

}
