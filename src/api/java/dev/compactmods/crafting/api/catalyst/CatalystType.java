package dev.compactmods.crafting.api.catalyst;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface CatalystType<Matcher extends ICatalystMatcher> extends IForgeRegistryEntry<CatalystType<?>> {

    Codec<Matcher> getCodec();
}
