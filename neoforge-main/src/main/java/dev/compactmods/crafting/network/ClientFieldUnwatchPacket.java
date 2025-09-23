package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record ClientFieldUnwatchPacket(BlockPos center) implements CustomPacketPayload {

    public static final Type<ClientFieldUnwatchPacket> TYPE = new Type<>(CompactCrafting.modRL("field_unwatch"));

    public static final StreamCodec<FriendlyByteBuf, ClientFieldUnwatchPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientFieldUnwatchPacket::center,
            ClientFieldUnwatchPacket::new
    );

    public static final IPayloadHandler<ClientFieldUnwatchPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandler.removeField(pkt.center);
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
