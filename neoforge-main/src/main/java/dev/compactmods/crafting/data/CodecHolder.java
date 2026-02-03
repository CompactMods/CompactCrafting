package dev.compactmods.crafting.data;

import com.mojang.serialization.Codec;

public interface CodecHolder<T> {

   Codec<T> codec();
}
