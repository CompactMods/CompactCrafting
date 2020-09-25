package com.robotgryphon.compactcrafting.core;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.blocks.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.blocks.tiles.FieldProjectorTile;
import com.robotgryphon.compactcrafting.items.FieldProjectorItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.robotgryphon.compactcrafting.CompactCrafting.MOD_ID;

public class Registration {
    // ================================================================================================================
    //   REGISTRIES
    // ================================================================================================================
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

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

    // ================================================================================================================
    //   ITEMS
    // ================================================================================================================
    public static final RegistryObject<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(FIELD_PROJECTOR_BLOCK.get(), new Item.Properties().group(CompactCrafting.ITEM_GROUP)));

    // ================================================================================================================
    //   TILE ENTITIES
    // ================================================================================================================
    public static final RegistryObject<TileEntityType<FieldProjectorTile>> FIELD_PROJECTOR_TILE = TILE_ENTITIES.register("field_projector", () ->
            TileEntityType.Builder
                    .create(FieldProjectorTile::new, FIELD_PROJECTOR_BLOCK.get())
                    .build(null));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILE_ENTITIES.register(eventBus);
    }
}
