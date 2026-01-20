package dev.compactmods.crafting.core;

import dev.compactmods.crafting.CompactCraftingCommon;
import dev.compactmods.crafting.projector.FieldProjectorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public class CCItems {

    // ================================================================================================================
    public static final DeferredItem<Item> FIELD_PROJECTOR_ITEM = CompactCraftingCommon.ITEMS
            .registerItem("field_projector", FieldProjectorItem::new);

    public static final DeferredItem<Item> PROJECTOR_DISH_ITEM = CompactCraftingCommon.ITEMS.registerSimpleItem("projector_dish");

    public static final DeferredItem<Item> BASE_ITEM = CompactCraftingCommon.ITEMS.registerSimpleItem("base");

//    public static final DeferredItem<Item> RESCAN_PROXY_ITEM = ITEMS.register("rescan_proxy", () ->
//            new FieldProxyItem(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));
//
//    public static final DeferredItem<Item> MATCH_PROXY_ITEM = ITEMS.register("match_proxy", () ->
//            new FieldProxyItem(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    // ================================================================================================================

    public static void init(IEventBus bus) {
        CompactCraftingCommon.ITEMS.register(bus);
    }
}
