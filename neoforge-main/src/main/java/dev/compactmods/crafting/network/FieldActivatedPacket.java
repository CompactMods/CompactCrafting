package dev.compactmods.crafting.network;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record FieldActivatedPacket(MiniaturizationFieldLocation location) implements CustomPacketPayload {

    public static final Type<FieldActivatedPacket> TYPE = new Type<>(CompactCrafting.identifier("field_activated"));

    static final StreamCodec<FriendlyByteBuf, FieldActivatedPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldLocation.STREAM_CODEC, FieldActivatedPacket::location,
            FieldActivatedPacket::new
    );

    public static final IPayloadHandler<FieldActivatedPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist().isClient()) {
                ClientPacketHandler.handleFieldActivation(pkt.location);
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
