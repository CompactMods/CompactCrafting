package com.robotgryphon.compactcrafting;

import com.robotgryphon.compactcrafting.client.ClientConfig;
import com.robotgryphon.compactcrafting.client.ClientSetup;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.recipes.components.ComponentRegistration;
import com.robotgryphon.compactcrafting.server.ServerConfig;
import com.robotgryphon.compactcrafting.ui.container.ContainerRegistration;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(CompactCrafting.MOD_ID);
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeMatcher");

    public static final String MOD_ID = "compactcrafting";

    public static final ItemGroup ITEM_GROUP = new CCItemGroup();

    public CompactCrafting() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        forgeBus.register(ClientSetup.class);

        modBus.addListener(this::setup);

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        Registration.init();
        ComponentRegistration.init(modBus);
        ContainerRegistration.init(modBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.initialize();
    }

    public static class CCItemGroup extends ItemGroup {
        public CCItemGroup() {
            super(CompactCrafting.MOD_ID);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.FIELD_PROJECTOR_ITEM.get());
        }
    }
}
