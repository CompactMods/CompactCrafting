package dev.compactmods.crafting;

import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCCatalystTypes;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCLayerTypes;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.core.CreativeTabs;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.server.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(CompactCrafting.MOD_ID);
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeLoader");

    public static final String MOD_ID = "compactcrafting";

    public CompactCrafting(IEventBus eventBus) {
        eventBus.addListener(this::setup);

        final var mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        mlCtx.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CCBlocks.init(eventBus);
        CCItems.init(eventBus);
        CCCatalystTypes.init(eventBus);
        CCLayerTypes.init(eventBus);
        CCMiniaturizationRecipes.init(eventBus);
        ComponentRegistration.init(eventBus);
        // ContainerRegistration.init(eventBus);
        CreativeTabs.init(eventBus);
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.initialize();

        var block = CCBlocks.FIELD_PROJECTOR_BLOCK.get();
    }
}
