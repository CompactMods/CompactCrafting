package dev.compactmods.crafting.recipes.catalyst;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.compactmods.crafting.Registration;
import dev.compactmods.crafting.api.catalyst.CatalystType;
import dev.compactmods.crafting.api.catalyst.ICatalystMatcher;
import net.minecraft.util.ResourceLocation;

public class CatalystMatcherCodec implements Codec<CatalystType<?>> {

    /**
     * You probably want {@link CatalystMatcherCodec#MATCHER_CODEC} instead.
     */
    public static final CatalystMatcherCodec INSTANCE = new CatalystMatcherCodec();
    public static final Codec<ICatalystMatcher> MATCHER_CODEC = INSTANCE.dispatchStable(ICatalystMatcher::getType, CatalystType::getCodec);

    @Override
    public <T> DataResult<T> encode(CatalystType<?> input, DynamicOps<T> ops, T prefix) {
        ResourceLocation key = input.getRegistryName();
        if(key == null)
            return DataResult.error("Unknown registry element " + input);

        T toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge);
    }

    @Override
    public <T> DataResult<Pair<CatalystType<?>, T>> decode(DynamicOps<T> ops, T input) {
        return ResourceLocation.CODEC.decode(ops, input).flatMap(CatalystMatcherCodec::handleDecodeResult);
    }

    private static <CatalystMatcher> DataResult<Pair<CatalystType<?>, CatalystMatcher>> handleDecodeResult(Pair<ResourceLocation, CatalystMatcher> pair) {
        ResourceLocation id = pair.getFirst();
        if(!Registration.CATALYST_TYPES.containsKey(id))
            return DataResult.error("Unknown registry key: " + id);

        return DataResult.success(pair.mapFirst(Registration.CATALYST_TYPES::getValue));
    }
}
