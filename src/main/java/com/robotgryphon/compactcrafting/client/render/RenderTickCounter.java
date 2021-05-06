package com.robotgryphon.compactcrafting.client.render;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT)
public class RenderTickCounter {
    public static int renderTicks = 0;
    private static long prevMillis = Util.getMillis();
    private static final int DEFAULT_60_INTERVAL = 1000 / 60;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        long millis = Util.getMillis();
        // Only add to the render ticks if the interval is greater than or equal to the interval for 60 FPS for consistency
        if ((millis - prevMillis) % 1000L >= DEFAULT_60_INTERVAL) {
            renderTicks++;
            prevMillis = millis;
        }
    }
}