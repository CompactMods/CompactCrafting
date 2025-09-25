package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ProxyDataResponsePacket(BlockPos proxyPos, BlockPos fieldCenter) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ProxyDataResponsePacket> TYPE = new CustomPacketPayload.Type<>(CompactCrafting.modRL("proxy_data_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProxyDataResponsePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ProxyDataResponsePacket::proxyPos,
            BlockPos.STREAM_CODEC, ProxyDataResponsePacket::fieldCenter,
            ProxyDataResponsePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ProxyDataResponsePacket packet, IPayloadContext context) {
        ClientPacketHandler.handleProxyData(packet.proxyPos, packet.fieldCenter);
    }
}