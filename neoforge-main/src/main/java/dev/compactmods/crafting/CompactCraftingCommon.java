package dev.compactmods.crafting;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.capability.FieldProjectorCapabilities;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.RecipeComponentType;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import dev.compactmods.crafting.capabilities.FieldProjectors;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.field.MiniaturizationFields;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.projector.FieldProjectorsCommon;
import dev.compactmods.crafting.recipes.MiniaturizationRecipes;
import dev.compactmods.crafting.recipes.components.RecipeComponents;
import dev.compactmods.crafting.server.ServerConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(CompactCrafting.MOD_ID)
public class CompactCraftingCommon {

    public static final DeferredRegister.Blocks BLOCKS;
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static final DeferredRegister.Items ITEMS;
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES;
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES;
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES;
    public static final DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS;
    public static final DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS;

    public static final Registry<RecipeLayerType<?>> RECIPE_LAYER_TYPES_REGISTRY;
    public static final Registry<RecipeComponentType<?>> RECIPE_COMPONENTS_REGISTRY;

    static {
        RECIPE_COMPONENTS = DeferredRegister.create(IRecipeComponent.RECIPE_COMPONENTS_ID, CompactCrafting.MOD_ID);
        RECIPE_LAYERS = DeferredRegister.create(IRecipeLayer.REGISTRY_ID, CompactCrafting.MOD_ID);
        ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactCrafting.MOD_ID);
        RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, CompactCrafting.MOD_ID);
        RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, CompactCrafting.MOD_ID);
        ITEMS = DeferredRegister.createItems(CompactCrafting.MOD_ID);
        BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactCrafting.MOD_ID);
        BLOCKS = DeferredRegister.createBlocks(CompactCrafting.MOD_ID);

        RECIPE_LAYER_TYPES_REGISTRY = RECIPE_LAYERS.makeRegistry(_ -> {
        });

        RECIPE_COMPONENTS_REGISTRY = RECIPE_COMPONENTS.makeRegistry(_ -> {
        });
    }

    public CompactCraftingCommon(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG);

        registerContent(modBus);

        modBus.addListener(CompactCraftingCommon::buildCreativeTabs);
        modBus.addListener(CompactCraftingCommon::registerCapabilities);
        modBus.addListener(NetworkHandler::onPacketRegistration);
    }

    private static void registerContent(IEventBus modBus) {
        MiniaturizationFields.prepare();
        FieldProjectorsCommon.prepare();
        MiniaturizationRecipes.prepare();
        RecipeComponents.prepare();

        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ITEMS.register(modBus);

        RECIPES.register(modBus);
        RECIPE_TYPES.register(modBus);
        RECIPE_LAYERS.register(modBus);
        RECIPE_COMPONENTS.register(modBus);

        ATTACHMENT_TYPES.register(modBus);
    }

    private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlock(
                FieldProjectorCapabilities.FIELD_PROJECTOR_CONTROL,
                FieldProjectors::resolve,
                FieldProjectorsCommon.FIELD_PROJECTOR_BLOCK.get()
        );
    }

    private static void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        final var tabKey = event.getTabKey();

        if(tabKey.equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.insertAfter(Items.LODESTONE.getDefaultInstance(), FieldProjectorsCommon.FIELD_PROJECTOR_ITEM.toStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if(tabKey.equals(CreativeModeTabs.INGREDIENTS)) {
            event.accept(FieldProjectorsCommon.BASE_ITEM);
            event.accept(FieldProjectorsCommon.PROJECTOR_DISH_ITEM);
        }
    }
}
