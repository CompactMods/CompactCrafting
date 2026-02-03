package dev.compactmods.crafting.data;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

@FunctionalInterface
public interface DataFilePathResolver<T> {

    Path compute(MinecraftServer server, T key);
}
