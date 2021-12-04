package dev.compactmods.crafting.core;

import java.util.function.Supplier;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.items.FieldProjectorItem;
import dev.compactmods.crafting.proxies.item.FieldProxyItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CCItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CompactCrafting.MOD_ID);

    // ================================================================================================================

    static final Supplier<Item.Properties> BASE_ITEM_PROPS = () -> new Item.Properties().tab(CompactCrafting.ITEM_GROUP);

    public static final RegistryObject<Item> FIELD_PROJECTOR_ITEM = ITEMS.register("field_projector", () ->
            new FieldProjectorItem(CCBlocks.FIELD_PROJECTOR_BLOCK.get(), BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> PROJECTOR_DISH_ITEM = ITEMS.register("projector_dish", () ->
            new Item(BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> BASE_ITEM = ITEMS.register("base", () ->
            new Item(BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> RESCAN_PROXY_ITEM = ITEMS.register("rescan_proxy", () ->
            new FieldProxyItem(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MATCH_PROXY_ITEM = ITEMS.register("match_proxy", () ->
            new FieldProxyItem(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(), BASE_ITEM_PROPS.get()));

    // ================================================================================================================

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
}
