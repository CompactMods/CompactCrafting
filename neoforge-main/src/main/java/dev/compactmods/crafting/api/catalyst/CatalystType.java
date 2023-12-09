package dev.compactmods.crafting.api.catalyst;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;

public interface CatalystType<Matcher extends ICatalystMatcher> {

    Codec<Matcher> getCodec();
}
