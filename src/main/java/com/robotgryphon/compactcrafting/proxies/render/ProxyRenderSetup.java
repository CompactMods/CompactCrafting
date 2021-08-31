package com.robotgryphon.compactcrafting.proxies.render;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
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
        // ClientRegistry.bindTileEntityRenderer(Registration.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);

        RenderTypeLookup.setRenderLayer(Registration.MATCH_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
        RenderTypeLookup.setRenderLayer(Registration.RESCAN_FIELD_PROXY_BLOCK.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onBlockColors(final ColorHandlerEvent.Block colors) {
        // color the ring at the base of the proxy poles
        colors.getBlockColors().register(new FieldProxyColors.MatchBlock(), Registration.MATCH_FIELD_PROXY_BLOCK.get());
        colors.getBlockColors().register(new FieldProxyColors.RescanBlock(), Registration.RESCAN_FIELD_PROXY_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(final ColorHandlerEvent.Item itemColors) {
        itemColors.getItemColors().register(new FieldProxyColors.MatchItem(), Registration.MATCH_PROXY_ITEM.get());
        itemColors.getItemColors().register(new FieldProxyColors.RescanItem(), Registration.RESCAN_PROXY_ITEM.get());
    }
}
