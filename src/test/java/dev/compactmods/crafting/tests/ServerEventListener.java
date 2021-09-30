package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

    @SubscribeEvent
    static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent evt) {
        final PlayerEntity player = evt.getPlayer();
        final MinecraftServer server = player.getServer();
        final PlayerList players = server.getPlayerList();
        final boolean op = players.isOp(player.getGameProfile());
        if(!op)
            players.op(player.getGameProfile());
    }
}
