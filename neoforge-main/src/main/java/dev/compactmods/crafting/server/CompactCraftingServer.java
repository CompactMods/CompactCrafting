package dev.compactmods.crafting.server;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.field.MiniaturizationFields;
import dev.compactmods.crafting.field.ServerFieldDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jspecify.annotations.Nullable;

@Mod(value = CompactCrafting.MOD_ID)
public class CompactCraftingServer {

    @Nullable
    private static MinecraftServer CURRENT_SERVER;

    @Nullable
    private static ServerFieldDataManager FIELD_DATA;

    public CompactCraftingServer() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactCraftingServer::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactCraftingServer::serverStarting);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactCraftingServer::serverStopping);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactCraftingServer::levelSaved);
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent evt) {
        CURRENT_SERVER = evt.getServer();
    }

    private static void serverStarting(final ServerStartingEvent evt) {
        final var server = evt.getServer();
        FIELD_DATA = new ServerFieldDataManager(server);
    }

    public static void saveAll() {
        if (CURRENT_SERVER != null) {
            if (FIELD_DATA != null) {
                FIELD_DATA.save();
            }
        }
    }

    public static void serverStopping(final ServerStoppingEvent ignored) {
        saveAll();
    }

    public static void levelSaved(final LevelEvent.Save level) {
        if(FIELD_DATA == null)
            return;

        if (level.getLevel() instanceof Level l) {
            l.getExistingData(MiniaturizationFields.ACTIVE_FIELDS)
                    .ifPresent(activeWorldFields -> activeWorldFields.save(FIELD_DATA));

            FIELD_DATA.save();
        }
    }
}
