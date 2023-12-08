package dev.compactmods.crafting.recipes.catalyst;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import dev.compactmods.crafting.core.CCCatalystTypes;
import dev.compactmods.crafting.util.CodecExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class CatalystMatcherCodec {

    private static final Codec<ICatalystMatcher> REGISTRY_TYPE_MATCHER_CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        final var catalystRegCodec = CCCatalystTypes.CATALYST_TYPES.get().getCodec();
        return catalystRegCodec.dispatchStable(ICatalystMatcher::getType, CatalystType::getCodec);
    });

    private final static Decoder<ICatalystMatcher> DECODER = new Decoder<>() {
        @Override
        public <T> DataResult<Pair<ICatalystMatcher, T>> decode(DynamicOps<T> ops, T input) {
            final var catalystType = ops.get(input, "type").result();
            if (catalystType.isEmpty()) {
                // Error: no catalyst type defined; falling back to the itemstack handler.
                return CodecExtensions.FRIENDLY_ITEMSTACK
                        .parse(ops, input)
                        .setLifecycle(Lifecycle.stable())
                        .map(is -> Pair.of(new ItemStackCatalystMatcher(is), input));

            } else {
                // type found, use key dispatch codec
                return REGISTRY_TYPE_MATCHER_CODEC
                        .parse(ops, input)
                        .setLifecycle(Lifecycle.stable())
                        .map(matcher -> Pair.of(matcher, input));
            }
        }
    };

    public static final Codec<ICatalystMatcher> MATCHER_CODEC = Codec.of(REGISTRY_TYPE_MATCHER_CODEC, DECODER);
}