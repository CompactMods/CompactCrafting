package com.robotgryphon.compactcrafting.core;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
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
    private static final DeferredRegister<TileEntityType<?>> TILES_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    // private static final DeferredRegister<Dimension> DIMENSIONS = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, MODID);;

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
//    public static final RegistryObject<Block> MACHINE_BLOCK_TINY = BLOCKS.register("machine_tiny", () ->
//            new BlockCompactMachine(EnumMachineSize.TINY, MACHINE_BLOCK_PROPS));

    // ================================================================================================================
    //   TILE ENTITIES
    // ================================================================================================================
//    public static final RegistryObject<TileEntityType<CompactMachineTile>> MACHINE_TILE_ENTITY = TILES_ENTITIES.register("compact_machine", () ->
//            TileEntityType.Builder.create(CompactMachineTile::new,
//                    MACHINE_BLOCK_TINY.get(), MACHINE_BLOCK_SMALL.get(), MACHINE_BLOCK_NORMAL.get(),
//                    MACHINE_BLOCK_NORMAL.get(), MACHINE_BLOCK_GIANT.get(), MACHINE_BLOCK_MAXIMUM.get())
//                    .build(null));

    // ================================================================================================================
    //   INITIALIZATION
    // ================================================================================================================
    public static void init() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        TILES_ENTITIES.register(eventBus);
    }
}
