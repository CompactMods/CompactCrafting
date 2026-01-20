package dev.compactmods.crafting.api.util;

import com.google.common.primitives.ImmutableDoubleArray;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.function.Function;

public interface CCExtraCodecs {
    Codec<Vector3d> VECTOR3D = Codec.DOUBLE.listOf()
            .comapFlatMap(input -> Util.fixedSize(input, 3).map(
                            d -> new Vector3d(ImmutableDoubleArray.copyOf(d).toArray())),
                    vec -> List.of(vec.x(), vec.y(), vec.z())
            );

    StreamCodec<FriendlyByteBuf, Vector3d> VECTOR3D_STREAM = StreamCodec.composite(
            ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list(3)),
            vec -> List.of(vec.x(), vec.y(), vec.z()),
            list -> new Vector3d(list.getFirst(), list.get(1), list.get(2))
    );

    Codec<Vector3dc> VECTOR3DC = VECTOR3D.xmap(Function.identity(), Vector3d::new);

    StreamCodec<FriendlyByteBuf, Vector3dc> VECTOR3DC_STREAM = VECTOR3D_STREAM.map(Function.identity(), Vector3d::new);
}
