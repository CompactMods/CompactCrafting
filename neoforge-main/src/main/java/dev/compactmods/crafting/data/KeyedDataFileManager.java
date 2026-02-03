package dev.compactmods.crafting.data;

import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.IOUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A codec-backed file that stores several instances of typed data, indexed by a key.
 *
 * @param <Key> The key used for instance lookups.
 * @param <T>
 */
public abstract class KeyedDataFileManager<Key, T extends CodecHolder<T>> {

    protected final MinecraftServer server;
    private final BiFunction<MinecraftServer, Key, T> creator;
    private final HashMap<Key, T> cache;

    public KeyedDataFileManager(MinecraftServer server, BiFunction<MinecraftServer, Key, T> creator) {
        this.server = server;
        this.creator = creator;
        this.cache = new HashMap<>();
    }

    public abstract DataFilePathResolver<Key> pathResolver();

    public T data(Key key) {
        return cache.computeIfAbsent(key, k -> {
            var inst = creator.apply(server, k);
            var file = pathResolver().compute(server, key).toFile();
            DataFileUtil.ensureDirExists(file.toPath().getParent());
            return !file.exists() ? inst : DataFileUtil.loadFileWithCodec(file, inst.codec());
        });
    }

    public Optional<T> optionalData(Key key) {
        return hasData(key) ? Optional.ofNullable(data(key)) : Optional.empty();
    }

    public void save() {
        cache.forEach((key, data) -> {
            var fullData = new CompoundTag();
            fullData.store("data", data.codec(), data);

            try {
                final var dataFilePath = pathResolver().compute(server, key).toAbsolutePath();
                final var dataFileLoc = dataFilePath.getParent();
                DataFileUtil.ensureDirExists(dataFileLoc);
                IOUtilities.writeNbtCompressed(fullData, dataFilePath);
            } catch (IOException e) {
                CompactCrafting.LOGGER.error("Failed to write data: {}", e.getMessage(), e);
            }
        });
    }

    public boolean hasData(Key key) {
        return cache.containsKey(key);
    }

    public void setData(Key key, T data) {
        cache.put(key, data);
    }
}
