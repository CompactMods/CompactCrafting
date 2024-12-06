package dev.compactmods.crafting;

import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCLayerTypes;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.core.CreativeTabs;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger(CompactCrafting.MOD_ID);
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeLoader");

    public static final String MOD_ID = "compactcrafting";

    public CompactCrafting(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CCBlocks.init(modBus);
        CCItems.init(modBus);
        CCLayerTypes.init(modBus);
        CCMiniaturizationRecipes.init(modBus);
        ComponentRegistration.init(modBus);
        // ContainerRegistration.init(eventBus);
        CreativeTabs.init(modBus);

        modBus.addListener(NetworkHandler::onPacketRegistration);
    }

    public static ResourceLocation modRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String rlPrefix(String path) {
        return CompactCrafting.MOD_ID + ":" + path;
    }
}
