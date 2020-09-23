package com.robotgryphon.compactcrafting.client.render;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderTickCounter {
    public static int renderTicks = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.RenderTickEvent.Phase.START) {
            renderTicks++;
        }
    }
}