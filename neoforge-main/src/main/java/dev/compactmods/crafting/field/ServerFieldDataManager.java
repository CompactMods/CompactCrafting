package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.data.DataFilePathResolver;
import dev.compactmods.crafting.data.KeyedDataFileManager;
import dev.compactmods.crafting.field.impl.CraftingState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.util.function.BiFunction;

public class ServerFieldDataManager extends KeyedDataFileManager<MiniaturizationFieldLocation, CraftingState> {

    private static final DataFilePathResolver<MiniaturizationFieldLocation> PATH_RESOLVER = (server, key) -> server
             .getWorldPath(LevelResource.ROOT)
             .resolve(CompactCrafting.MOD_ID)
             .resolve("fields")
             .resolve(key.dimension().identifier().getNamespace())
             .resolve(key.dimension().identifier().getPath())
             .resolve(key.centerBlock().asLong() + ".dat");

    private static final BiFunction<MinecraftServer, MiniaturizationFieldLocation, CraftingState> INITIALIZER
            = (_, _) -> CraftingState.create();

    public ServerFieldDataManager(MinecraftServer server) {
        super(server, INITIALIZER);
    }

    @Override
    public DataFilePathResolver<MiniaturizationFieldLocation> pathResolver() {
        return PATH_RESOLVER;
    }
}
