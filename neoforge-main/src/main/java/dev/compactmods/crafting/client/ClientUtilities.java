package dev.compactmods.crafting.client;

import net.minecraft.client.Minecraft;

public class ClientUtilities {

    public static boolean isDebugScreenOpen() {
        return Minecraft.getInstance().getDebugOverlay().showDebugScreen();
    }
}
