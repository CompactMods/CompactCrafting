package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestProxyDataPacket(BlockPos proxyPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestProxyDataPacket> TYPE = new CustomPacketPayload.Type<>(CompactCrafting.modRL("request_proxy_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestProxyDataPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RequestProxyDataPacket::proxyPos,
            RequestProxyDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestProxyDataPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            var blockEntity = serverPlayer.level().getBlockEntity(packet.proxyPos);
            if (blockEntity instanceof BaseFieldProxyEntity proxy) {
                PacketDistributor.sendToPlayer(serverPlayer, new ProxyDataResponsePacket(packet.proxyPos, proxy.fieldCenter, proxy.getProxyId()));
            }
        }
    }
}