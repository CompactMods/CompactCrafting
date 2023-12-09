package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.items.FieldProjectorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CCItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactCrafting.MOD_ID);

    // ================================================================================================================

    static final Supplier<Item.Properties> BASE_ITEM_PROPS = Item.Properties::new;

    public static final DeferredItem<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(CCBlocks.FIELD_PROJECTOR_BLOCK.get(), BASE_ITEM_PROPS.get()));

    public static final DeferredItem<Item> PROJECTOR_DISH_ITEM = ITEMS.register("projector_dish", () ->
            new Item(BASE_ITEM_PROPS.get()));

    public static final DeferredItem<Item> BASE_ITEM = ITEMS.register("base", () ->
            new Item(BASE_ITEM_PROPS.get()));

//    public static final DeferredItem<Item> RESCAN_PROXY_ITEM = ITEMS.register("rescan_proxy", () ->
//            new FieldProxyItem(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));
//
//    public static final DeferredItem<Item> MATCH_PROXY_ITEM = ITEMS.register("match_proxy", () ->
//            new FieldProxyItem(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    // ================================================================================================================

    public static void init(IEventBus bus) {
//        ITEMS.register(bus);
    }
}
