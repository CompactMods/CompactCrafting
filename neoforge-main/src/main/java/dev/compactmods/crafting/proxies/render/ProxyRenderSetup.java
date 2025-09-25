package dev.compactmods.crafting.proxies.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ProxyRenderSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        // color the ring at the base of the proxy poles
        colors.register(new FieldProxyColors.MatchBlock(), CCBlocks.MATCH_FIELD_PROXY_BLOCK.get());
        colors.register(new FieldProxyColors.RescanBlock(), CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item itemColors) {
        itemColors.register(new FieldProxyColors.MatchItem(), CCItems.MATCH_PROXY_ITEM.get());
        itemColors.register(new FieldProxyColors.RescanItem(), CCItems.RESCAN_PROXY_ITEM.get());
    }
}