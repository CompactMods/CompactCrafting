package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record ProxyDataResponsePacket(BlockPos proxyPos, @Nullable BlockPos fieldCenter, UUID proxyId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ProxyDataResponsePacket> TYPE = new CustomPacketPayload.Type<>(CompactCrafting.modRL("proxy_data_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProxyDataResponsePacket> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ProxyDataResponsePacket>() {
        @Override
        public ProxyDataResponsePacket decode(RegistryFriendlyByteBuf buf) {
            BlockPos proxyPos = BlockPos.STREAM_CODEC.decode(buf);
            BlockPos fieldCenter = buf.readBoolean() ? BlockPos.STREAM_CODEC.decode(buf) : null;
            UUID proxyId = buf.readUUID();
            return new ProxyDataResponsePacket(proxyPos, fieldCenter, proxyId);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ProxyDataResponsePacket packet) {
            BlockPos.STREAM_CODEC.encode(buf, packet.proxyPos);
            buf.writeBoolean(packet.fieldCenter != null);
            if (packet.fieldCenter != null) {
                BlockPos.STREAM_CODEC.encode(buf, packet.fieldCenter);
            }
            buf.writeUUID(packet.proxyId);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ProxyDataResponsePacket packet, IPayloadContext context) {
        ClientPacketHandler.handleProxyData(packet.proxyPos, packet.fieldCenter, packet.proxyId);
    }
}