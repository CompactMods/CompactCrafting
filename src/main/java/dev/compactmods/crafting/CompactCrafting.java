package dev.compactmods.crafting;

import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.ui.container.ContainerRegistration;
import dev.compactmods.crafting.core.*;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeLoader");

    public static final String MOD_ID = "compactcrafting";

    public static final CreativeModeTab ITEM_GROUP = new CCItemGroup();

    public CompactCrafting() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::setup);

        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CCBlocks.init(modBus);
        CCCatalystTypes.init(modBus);
        CCItems.init(modBus);
        CCLayerTypes.init(modBus);
        CCMiniaturizationRecipes.init(modBus);

        ComponentRegistration.init(modBus);
        ContainerRegistration.init(modBus);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.initialize();
    }

    public static class CCItemGroup extends CreativeModeTab {
        public CCItemGroup() {
            super(CompactCrafting.MOD_ID);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(CCItems.FIELD_PROJECTOR_ITEM.get());
        }
    }
}
