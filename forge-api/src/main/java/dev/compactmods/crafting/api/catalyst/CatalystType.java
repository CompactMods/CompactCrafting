package dev.compactmods.crafting.api.catalyst;

import com.mojang.serialization.Codec;

public interface CatalystType<Matcher extends ICatalystMatcher> {

    Codec<Matcher> getCodec();
}
