package com.robotgryphon.compactcrafting.core;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldCraftingPreviewBlock;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.inventory.MiniaturizationRecipeContainer;
import com.robotgryphon.compactcrafting.item.FieldProjectorItem;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import com.robotgryphon.compactcrafting.recipes.components.RecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.components.SimpleRecipeComponentType;
import com.robotgryphon.compactcrafting.recipes.data.MiniaturizationRecipeSerializer;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayerMatcher;
import com.robotgryphon.compactcrafting.recipes.layers.RecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.SimpleRecipeLayerType;
import com.robotgryphon.compactcrafting.recipes.layers.impl.FilledComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.HollowComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.layers.impl.MixedComponentRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.setup.BaseRecipeType;
import com.robotgryphon.compactcrafting.tiles.DummyFieldProjectorTile;
import com.robotgryphon.compactcrafting.tiles.FieldCraftingPreviewTile;
import com.robotgryphon.compactcrafting.tiles.MainFieldProjectorTile;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class Registration {
    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CompactCrafting.MOD_ID);
    private static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CompactCrafting.MOD_ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CompactCrafting.MOD_ID);
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final DeferredRegister<RecipeLayerType<?>> RECIPE_LAYERS = DeferredRegister.create((Class) RecipeLayerType.class, CompactCrafting.MOD_ID);
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final DeferredRegister<IRecipeLayerMatcher<?>> RECIPE_LAYER_MATCHERS = DeferredRegister.create((Class) IRecipeLayerMatcher.class, CompactCrafting.MOD_ID);
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final DeferredRegister<RecipeComponentType<?>> RECIPE_COMPONENTS = DeferredRegister.create((Class) RecipeComponentType.class, CompactCrafting.MOD_ID);
    private static final Supplier<IForgeRegistry<RecipeLayerType<?>>> RECIPE_LAYERS_REGISTRY = makeRegistry(RECIPE_LAYERS, "recipe_layers");
    private static final Supplier<IForgeRegistry<IRecipeLayerMatcher<?>>> RECIPE_LAYER_MATCHERS_REGISTRY = makeRegistry(RECIPE_LAYER_MATCHERS, "recipe_matchers");
    private static final Supplier<IForgeRegistry<RecipeComponentType<?>>> RECIPE_COMPONENTS_REGISTRY = makeRegistry(RECIPE_COMPONENTS, "recipe_components");

    // ================================================================================================================
    //   PROPERTIES
    // ================================================================================================================
    // private static Block.Properties MACHINE_BLOCK_PROPS = Block.Properties
    //         .of(Material.METAL)
    //         .strength(8.0F, 20.0F)
    //         .lightLevel(state -> 1)
    //         .harvestLevel(1)
    //         .harvestTool(ToolType.PICKAXE)
    //         .requiresCorrectToolForDrops();

    // ================================================================================================================
    //   BLOCKS
    // ================================================================================================================
    public static final RegistryObject<Block> FIELD_PROJECTOR_BLOCK = BLOCKS.register("field_projector", () ->
            new FieldProjectorBlock(AbstractBlock.Properties.of(Material.METAL)
                    .strength(8, 20)
            ));

    public static final RegistryObject<Block> FIELD_CRAFTING_PREVIEW_BLOCK = BLOCKS.register("field_crafting_preview", () ->
            new FieldCraftingPreviewBlock(AbstractBlock.Properties.of(Material.BARRIER)
                    .noCollission()
                    .noDrops()
                    .noOcclusion()
            ));

    // ================================================================================================================
    //   ITEMS
    // ================================================================================================================
    public static final RegistryObject<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(FIELD_PROJECTOR_BLOCK.get(), new Item.Properties().tab(CompactCrafting.ITEM_GROUP)));

    // ================================================================================================================
    //   TILE ENTITIES
    // ================================================================================================================
    public static final RegistryObject<TileEntityType<MainFieldProjectorTile>> MAIN_FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("main_field_projector", () ->
            TileEntityType.Builder
                    .of(MainFieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<DummyFieldProjectorTile>> DUMMY_FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("dummy_field_projector", () ->
            TileEntityType.Builder
                    .of(DummyFieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    public static final RegistryObject<TileEntityType<FieldCraftingPreviewTile>> FIELD_CRAFTING_PREVIEW_TILE = TILE_ENTITIES.register("field_crafting_preview", () ->
            TileEntityType.Builder
                    .of(FieldCraftingPreviewTile::new, FIELD_CRAFTING_PREVIEW_BLOCK.get())
                    .build(null));

    // ================================================================================================================
    //   SOUNDS
    // ================================================================================================================
    public static final RegistryObject<SoundEvent> MINIATURIZATION_CRAFTING_SOUND = registerSound("miniaturization_crafting");

    // ================================================================================================================
    //   CONTAINER TYPES
    // ================================================================================================================
    public static final RegistryObject<ContainerType<MiniaturizationRecipeContainer>> MINIATURIZATION_RECIPE_CONTAINER_TYPE = CONTAINERS.register("miniaturization", () ->
            IForgeContainerType.create(MiniaturizationRecipeContainer::new));

    // ================================================================================================================
    //   MINIATURIZATION RECIPES
    // ================================================================================================================
    public static final RegistryObject<IRecipeSerializer<MiniaturizationRecipe>> MINIATURIZATION_SERIALIZER = RECIPES.register("miniaturization", MiniaturizationRecipeSerializer::new);

    public static final BaseRecipeType<MiniaturizationRecipe> MINIATURIZATION_RECIPE_TYPE = new BaseRecipeType<>(MINIATURIZATION_SERIALIZER);

    // ================================================================================================================
    //   RECIPE LAYER TYPES
    // ================================================================================================================
    public static final RegistryObject<RecipeLayerType<FilledComponentRecipeLayer>> FILLED_LAYER_TYPE =
            RECIPE_LAYERS.register("filled", () -> new SimpleRecipeLayerType<>(FilledComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<HollowComponentRecipeLayer>> HOLLOW_LAYER_TYPE =
            RECIPE_LAYERS.register("hollow", () -> new SimpleRecipeLayerType<>(HollowComponentRecipeLayer.CODEC));

    public static final RegistryObject<RecipeLayerType<MixedComponentRecipeLayer>> MIXED_LAYER_TYPE =
            RECIPE_LAYERS.register("mixed", () -> new SimpleRecipeLayerType<>(MixedComponentRecipeLayer.CODEC));

    // ================================================================================================================
    //   RECIPE LAYER MATCHERS
    // ================================================================================================================
    public static final RegistryObject<IRecipeLayerMatcher<FilledComponentRecipeLayer>> FILLED_LAYER_MATCHER =
            RECIPE_LAYER_MATCHERS.register("filled", FilledComponentRecipeLayer.Matcher::new);

    public static final RegistryObject<IRecipeLayerMatcher<HollowComponentRecipeLayer>> HOLLOW_LAYER_MATCHER =
            RECIPE_LAYER_MATCHERS.register("hollow", HollowComponentRecipeLayer.Matcher::new);

    public static final RegistryObject<IRecipeLayerMatcher<MixedComponentRecipeLayer>> MIXED_LAYER_MATCHER =
            RECIPE_LAYER_MATCHERS.register("mixed", MixedComponentRecipeLayer.Matcher::new);

    // ================================================================================================================
    //   RECIPE COMPONENTS
    // ================================================================================================================
    public static final RegistryObject<RecipeComponentType<RecipeBlockStateComponent>> BLOCKSTATE_COMPONENT =
            RECIPE_COMPONENTS.register("block", () -> new SimpleRecipeComponentType<>(RecipeBlockStateComponent.CODEC));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
        RECIPES.register(eventBus);
        SOUNDS.register(eventBus);
        CONTAINERS.register(eventBus);

        // Recipe Types (Forge Registry setup does not call this yet)
        MINIATURIZATION_RECIPE_TYPE.register();

        RECIPE_LAYERS.register(eventBus);
        RECIPE_LAYER_MATCHERS.register(eventBus);
        RECIPE_COMPONENTS.register(eventBus);
    }

    private Registration() {}

    private static <T extends IForgeRegistryEntry<T>> Supplier<IForgeRegistry<T>> makeRegistry(DeferredRegister<T> register, String name) {
        return register.makeRegistry(name, () -> new RegistryBuilder<T>().tagFolder(name));
    }

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(CompactCrafting.MOD_ID, name)));
    }

    public static IForgeRegistry<RecipeLayerType<?>> getRecipeLayerTypes() {
        return RECIPE_LAYERS_REGISTRY.get();
    }

    public static IForgeRegistry<RecipeComponentType<?>> getRecipeComponentTypes() {
        return RECIPE_COMPONENTS_REGISTRY.get();
    }

    public static IForgeRegistry<IRecipeLayerMatcher<?>> getRecipeLayerMatchers() {
        return RECIPE_LAYER_MATCHERS_REGISTRY.get();
    }
}
