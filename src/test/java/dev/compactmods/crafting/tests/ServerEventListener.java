package dev.compactmods.crafting.tests;

import java.io.File;
import java.util.concurrent.ExecutionException;
import com.google.common.collect.ImmutableSet;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.ResourcePackList;
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

        // Add "test/resources" as a resource pack to the pack repository
        final ResourcePackList packs = server.getPackRepository();
        final FolderPackFinder testPack = new FolderPackFinder(new File(System.getenv("TEST_RESOURCES")), IPackNameDecorator.DEFAULT);
        packs.addPackFinder(testPack);
        packs.reload();

        // add "file/resources" to selected pack list
        final ImmutableSet<String> toSelect = ImmutableSet.<String>builder()
                .addAll(packs.getSelectedIds())
                .add("file/test_data")
                .build();

        packs.setSelected(toSelect);
        
        try {
            server.reloadResources(packs.getSelectedIds()).get();
        } catch (InterruptedException | ExecutionException e) {
            CompactCrafting.LOGGER.error("Failed to reload test resource packs.", e);
        }
    }

    @SubscribeEvent
    static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent evt) {
        final PlayerEntity player = evt.getPlayer();
        final MinecraftServer server = player.getServer();
        final PlayerList players = server.getPlayerList();
        final boolean op = players.isOp(player.getGameProfile());
        if (!op)
            players.op(player.getGameProfile());
    }
}
