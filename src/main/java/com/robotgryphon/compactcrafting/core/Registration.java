package com.robotgryphon.compactcrafting.core;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.*;
import com.robotgryphon.compactcrafting.items.FieldProjectorItem;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.data.MiniaturizationRecipeSerializer;
import com.robotgryphon.compactcrafting.recipes.data.base.BaseRecipeType;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.FilledLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.HollowLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.MixedLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.data.serialization.layers.RecipeLayerSerializer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.FilledComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.HollowComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.MixedComponentRecipeLayer;
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
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

import static com.robotgryphon.compactcrafting.CompactCrafting.MOD_ID;

@SuppressWarnings("unchecked")
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================


    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    private static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static DeferredRegister<RecipeLayerSerializer<?>> RECIPE_LAYERS = DeferredRegister.create((Class) RecipeLayerSerializer.class, MOD_ID);
    public static IForgeRegistry<RecipeLayerSerializer<?>> RECIPE_SERIALIZERS;

    static {
        RECIPE_LAYERS.makeRegistry("recipe_layer_serializers", () -> new RegistryBuilder<RecipeLayerSerializer<?>>()
                .setName(new ResourceLocation(MOD_ID, "recipe_layer_serializers"))
                .setType(c(RecipeLayerSerializer.class))
                .tagFolder("recipe_layer_serializers"));
    }

    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    private static Block.Properties MACHINE_BLOCK_PROPS = Block.Properties
            .create(Material.IRON)
            .hardnessAndResistance(8.0F, 20.0F)
            .setLightLevel(state -> 1)
            .harvestLevel(1)
            .harvestTool(ToolType.PICKAXE)
            .setRequiresTool();

    private static Supplier<Item.Properties> BASIC_ITEM_PROPS = () -> new Item.Properties()
            .group(CompactCrafting.ITEM_GROUP);

    // ================================================================================================================
    //   BLOCKS
    // ================================================================================================================
    public static final RegistryObject<Block> FIELD_PROJECTOR_BLOCK = BLOCKS.register("field_projector", () ->
            new FieldProjectorBlock(Block.Properties.create(Material.IRON)
                    .hardnessAndResistance(8, 20)
            ));

    public static final RegistryObject<Block> FIELD_CRAFTING_PREVIEW_BLOCK = BLOCKS.register("field_crafting_preview", () ->
            new FieldCraftingPreviewBlock(AbstractBlock.Properties.from(Blocks.BARRIER)));

    // ================================================================================================================
    //   ITEMS
    // ================================================================================================================
    public static final RegistryObject<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(FIELD_PROJECTOR_BLOCK.get(), new Item.Properties().group(CompactCrafting.ITEM_GROUP)));

    // ================================================================================================================
    //   TILE ENTITIES
    // ================================================================================================================
    public static final RegistryObject<TileEntityType<MainFieldProjectorTile>> FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("main_field_projector", () ->
            TileEntityType.Builder
                    .create(MainFieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<DummyFieldProjectorTile>> DUMMY_FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("dummy_field_projector", () ->
            TileEntityType.Builder
                    .create(DummyFieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<FieldCraftingPreviewTile>> FIELD_CRAFTING_PREVIEW_TILE = TILE_ENTITIES.register("field_crafting_preview", () ->
            TileEntityType.Builder
                    .create(FieldCraftingPreviewTile::new, FIELD_CRAFTING_PREVIEW_BLOCK.get())
                    .build(null));

    // ================================================================================================================
    //   MINIATURIZATION RECIPES
    // ================================================================================================================
    public static final RegistryObject<IRecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER = RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final ResourceLocation MINIATURIZATION_RECIPE_TYPE_ID = new ResourceLocation(MOD_ID, "miniaturization_recipe");

    public static final BaseRecipeType<MiniaturizationRecipe> MINIATURIZATION_RECIPE_TYPE = new BaseRecipeType<>(MINIATURIZATION_RECIPE_TYPE_ID);

    // ================================================================================================================
    //   RECIPE LAYER SERIALIZERS
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerSerializer<FilledComponentRecipeLayer>> FILLED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("filled", FilledLayerSerializer::new);

    public static final RegistryObject<RecipeLayerSerializer<HollowComponentRecipeLayer>> HOLLOW_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("hollow", HollowLayerSerializer::new);

    public static final RegistryObject<RecipeLayerSerializer<MixedComponentRecipeLayer>> MIXED_LAYER_SERIALIZER =
            RECIPE_LAYERS.register("mixed", MixedLayerSerializer::new);

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    private static <T> Class<T> c(Class<?> cls) { return (Class<T>)cls; }

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
    public static void onRegistration(RegistryEvent.Register<RecipeLayerSerializer<?>> evt) {
        RECIPE_SERIALIZERS = evt.getRegistry();
    }


}
