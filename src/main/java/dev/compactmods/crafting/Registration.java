package dev.compactmods.crafting;

import java.util.function.Supplier;
import dev.compactmods.crafting.field.block.FieldCraftingPreviewBlock;
import dev.compactmods.crafting.field.tile.FieldCraftingPreviewTile;
import dev.compactmods.crafting.items.FieldProjectorItem;
import dev.compactmods.crafting.projector.block.FieldProjectorBlock;
import dev.compactmods.crafting.projector.tile.FieldProjectorTile;
import dev.compactmods.crafting.proxies.block.MatchFieldProxyBlock;
import dev.compactmods.crafting.proxies.block.RescanFieldProxyBlock;
import dev.compactmods.crafting.proxies.data.MatchFieldProxyEntity;
import dev.compactmods.crafting.proxies.data.RescanFieldProxyEntity;
import dev.compactmods.crafting.proxies.item.FieldProxyItem;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipeSerializer;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.recipes.layers.SimpleRecipeLayerType;
import dev.compactmods.crafting.recipes.setup.BaseRecipeType;
import dev.compactmods.crafting.api.recipe.layers.RecipeLayerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================


    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CompactCrafting.MOD_ID);
    private static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CompactCrafting.MOD_ID);

    public static DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create((Class) RecipeLayerType.class, CompactCrafting.MOD_ID);
    public static IForgeRegistry<RecipeLayerType<?>> RECIPE_LAYER_TYPES;

    static {
        RECIPE_LAYERS.makeRegistry("recipe_layers", () -> new RegistryBuilder<RecipeLayerType<?>>()
                .tagFolder("recipe_layers"));
    }

    // ================================================================================================================
    // region  PROPERTIES
    // ================================================================================================================
    static final Supplier<AbstractBlock.Properties> PROXY_PROPS = () -> AbstractBlock.Properties.of(Material.HEAVY_METAL)
            .strength(8, 20)
            .requiresCorrectToolForDrops()
            .harvestTool(ToolType.PICKAXE);

    static final Supplier<Item.Properties> BASE_ITEM_PROPS = () -> new Item.Properties().tab(CompactCrafting.ITEM_GROUP);

    // endregion ======================================================================================================

    // ================================================================================================================
    // region  BLOCKS
    // ================================================================================================================
    public static final RegistryObject<Block> FIELD_PROJECTOR_BLOCK = BLOCKS.register("field_projector", () ->
            new FieldProjectorBlock(AbstractBlock.Properties.of(Material.METAL)
                    .strength(8, 20)
            ));

    public static final RegistryObject<Block> FIELD_CRAFTING_PREVIEW_BLOCK = BLOCKS.register("field_crafting_preview", () ->
            new FieldCraftingPreviewBlock(AbstractBlock.Properties.copy(Blocks.BARRIER)));

    public static final RegistryObject<Block> RESCAN_FIELD_PROXY_BLOCK = BLOCKS.register("rescan_proxy", () ->
            new RescanFieldProxyBlock(PROXY_PROPS.get()));

    public static final RegistryObject<Block> MATCH_FIELD_PROXY_BLOCK = BLOCKS.register("match_proxy", () ->
            new MatchFieldProxyBlock(PROXY_PROPS.get()));

    // endregion ======================================================================================================

    // ================================================================================================================
    // region ITEMS
    // ================================================================================================================
    public static final RegistryObject<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(FIELD_PROJECTOR_BLOCK.get(), BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> PROJECTOR_DISH_ITEM = ITEMS.register("projector_dish", () ->
            new Item(BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> BASE_ITEM = ITEMS.register("base", () ->
            new Item(BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> RESCAN_PROXY_ITEM = ITEMS.register("rescan_proxy", () ->
            new FieldProxyItem(RESCAN_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MATCH_PROXY_ITEM = ITEMS.register("match_proxy", () ->
                new FieldProxyItem(MATCH_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    // endregion ======================================================================================================

    // ================================================================================================================
    // region  TILE ENTITIES
    // ================================================================================================================
    public static final RegistryObject<TileEntityType<FieldProjectorTile>> FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("field_projector", () ->
            TileEntityType.Builder
                    .of(FieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<FieldCraftingPreviewTile>> FIELD_CRAFTING_PREVIEW_TILE = TILE_ENTITIES.register("field_crafting_preview", () ->
            TileEntityType.Builder
                    .of(FieldCraftingPreviewTile::new, FIELD_CRAFTING_PREVIEW_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<RescanFieldProxyEntity>> RESCAN_PROXY_ENTITY = TILE_ENTITIES.register("rescan_proxy", () ->
            TileEntityType.Builder
                    .of(RescanFieldProxyEntity::new, RESCAN_FIELD_PROXY_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<MatchFieldProxyEntity>> MATCH_PROXY_ENTITY = TILE_ENTITIES.register("match_proxy", () ->
            TileEntityType.Builder
                    .of(MatchFieldProxyEntity::new, MATCH_FIELD_PROXY_BLOCK.get())
                    .build(null));

    // endregion ======================================================================================================

    // ================================================================================================================
    // region  MINIATURIZATION RECIPES
    // ================================================================================================================
    public static final RegistryObject<IRecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER = RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final ResourceLocation MINIATURIZATION_RECIPE_TYPE_ID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization_recipe");

    public static final BaseRecipeType<MiniaturizationRecipe> MINIATURIZATION_RECIPE_TYPE = new BaseRecipeType<>(MINIATURIZATION_RECIPE_TYPE_ID);

    // endregion ======================================================================================================

    // ================================================================================================================
    // region  RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", () -> new SimpleRecipeLayerType<>(FilledComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", () -> new SimpleRecipeLayerType<>(HollowComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", () -> new SimpleRecipeLayerType<>(MixedComponentRecipeLayer.CODEC));

    // endregion ======================================================================================================

    // region =========================================================================================================

    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
        RECIPES.register(eventBus);

        // Recipe Types (Forge Registry setup does not call this yet)
        MINIATURIZATION_RECIPE_TYPE.register();

        RECIPE_LAYERS.register(eventBus);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onRegistration(RegistryEvent.Register<RecipeLayerType<?>> evt) {
        RECIPE_LAYER_TYPES = evt.getRegistry();
    }
    // endregion ======================================================================================================
}
