package dev.compactmods.crafting.proxies.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ProxyRenderSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.MATCH_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onBlockColors(final ColorHandlerEvent.Block colors) {
        // color the ring at the base of the proxy poles
        colors.getBlockColors().register(new FieldProxyColors.MatchBlock(), CCBlocks.MATCH_FIELD_PROXY_BLOCK.get());
        colors.getBlockColors().register(new FieldProxyColors.RescanBlock(), CCBlocks.RESCAN_FIELD_PROXY_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(final ColorHandlerEvent.Item itemColors) {
        itemColors.getItemColors().register(new FieldProxyColors.MatchItem(), CCItems.MATCH_PROXY_ITEM.get());
        itemColors.getItemColors().register(new FieldProxyColors.RescanItem(), CCItems.RESCAN_PROXY_ITEM.get());
    }
}
