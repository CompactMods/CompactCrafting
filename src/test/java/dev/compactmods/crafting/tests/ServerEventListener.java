package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class ServerEventListener {
    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent evt) {

        final MinecraftServer server = evt.getServer();
        // final File root = server.getServerDirectory().getParentFile();
        // server.getPackRepository().addPackFinder(new FolderPackFinder());
    }

}
