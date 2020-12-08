package com.robotgryphon.compactcrafting.util;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public abstract class JsonUtil {

    public static Optional<ItemStack> getItemStack(JsonObject json) {
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, json)
                .get()
                .ifRight(err -> CompactCrafting.LOGGER.warn("Failed to load itemstack from JSON: {}", err.message()))
                .mapLeft(Pair::getFirst)
                .left();
    }

    public static Optional<BlockState> getBlockState(JsonObject json) {
        return BlockState.CODEC.decode(JsonOps.INSTANCE, json)
                .get().ifRight(error -> {
                    CompactCrafting.LOGGER.warn("Failed to load blockstate from JSON: {}", error.message());
                }).mapLeft(Pair::getFirst).left();
    }
}
