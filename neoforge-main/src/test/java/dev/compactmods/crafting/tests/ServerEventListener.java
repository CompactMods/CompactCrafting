package dev.compactmods.crafting.tests;

import java.io.File;
import java.util.concurrent.ExecutionException;
import com.google.common.collect.ImmutableSet;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID)
public class ServerEventListener {

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent evt) {
        final MinecraftServer server = evt.getServer();

        // Add "test/resources" as a resource pack to the pack repository
        final var packs = server.getPackRepository();

        final String cc_test_resources = System.getenv("CC_TEST_RESOURCES");
        if(cc_test_resources != null) {
            final var testPack = new FolderRepositorySource(new File(cc_test_resources), PackSource.DEFAULT);
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
    }

    @SubscribeEvent
    static void onPlayerJoined(final PlayerEvent.PlayerLoggedInEvent evt) {
        final var player = evt.getEntity();
        final var server = player.getServer();
        final var players = server.getPlayerList();
        final boolean op = players.isOp(player.getGameProfile());
        if (!op)
            players.op(player.getGameProfile());
    }
}
