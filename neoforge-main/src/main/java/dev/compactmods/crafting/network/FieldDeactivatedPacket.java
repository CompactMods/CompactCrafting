package dev.compactmods.crafting.network;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldLocation;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record FieldDeactivatedPacket(MiniaturizationFieldLocation location) implements CustomPacketPayload {

    public static final Type<FieldDeactivatedPacket> TYPE = new Type<>(CompactCrafting.identifier("field_deactivated"));

    static final StreamCodec<FriendlyByteBuf, FieldDeactivatedPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldLocation.STREAM_CODEC, FieldDeactivatedPacket::location,
            FieldDeactivatedPacket::new
    );

    public static final IPayloadHandler<FieldDeactivatedPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist().isClient()) {
                ClientPacketHandler.handleFieldDeactivation(pkt.location());
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
