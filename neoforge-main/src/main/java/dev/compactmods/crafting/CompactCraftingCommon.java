package dev.compactmods.crafting;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.capability.FieldProjectorCapabilities;
import dev.compactmods.crafting.capabilities.FieldProjectors;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.core.CCAttachments;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import dev.compactmods.crafting.core.CCLayerTypes;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.core.CreativeTabs;
import dev.compactmods.crafting.events.BlockEventHandler;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.recipes.components.ComponentRegistration;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CompactCrafting.MOD_ID)
public class CompactCraftingCommon {

    public static final DeferredRegister.Blocks BLOCKS
            = DeferredRegister.createBlocks(CompactCrafting.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES
            = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactCrafting.MOD_ID);

    public static final DeferredRegister<PositionSourceType<?>> GAME_EVENT_POSITION_SOURCES
            = DeferredRegister.create(BuiltInRegistries.POSITION_SOURCE_TYPE, CompactCrafting.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactCrafting.MOD_ID);

    public CompactCraftingCommon(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        CCBlocks.init(modBus);
        CCItems.init(modBus);
        CCLayerTypes.init(modBus);
        CCMiniaturizationRecipes.init(modBus);
        ComponentRegistration.init(modBus);
        // ContainerRegistration.init(eventBus);
        CreativeTabs.init(modBus);
        CCAttachments.ATTACHMENT_TYPES.register(modBus);

        NeoForge.EVENT_BUS.addListener(BlockEventHandler::onRightClickBlock);

        modBus.addListener(CompactCraftingCommon::registerCapabilities);
        modBus.addListener(NetworkHandler::onPacketRegistration);

    }

    private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlock(
                FieldProjectorCapabilities.FIELD_PROJECTOR_CONTROL,
                FieldProjectors::resolve,
                CCBlocks.FIELD_PROJECTOR_BLOCK.get()
        );
    }
}
