package dev.compactmods.crafting.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void onPacketRegistration(final RegisterPayloadHandlersEvent payloads) {
        final PayloadRegistrar main = payloads.registrar("2.0.0");

        main.playToClient(FieldDeactivatedPacket.TYPE, FieldDeactivatedPacket.STREAM_CODEC, FieldDeactivatedPacket.HANDLER);
        main.playToClient(FieldActivatedPacket.TYPE, FieldActivatedPacket.STREAM_CODEC, FieldActivatedPacket.HANDLER);

        main.playToClient(ClientFieldWatchPacket.TYPE, ClientFieldWatchPacket.STREAM_CODEC, ClientFieldWatchPacket.HANDLER);
        main.playToClient(ClientFieldUnwatchPacket.TYPE, ClientFieldUnwatchPacket.STREAM_CODEC, ClientFieldUnwatchPacket.HANDLER);

        main.playToClient(FieldRecipeChangedPacket.TYPE, FieldRecipeChangedPacket.STREAM_CODEC, FieldRecipeChangedPacket.HANDLER);
        main.playToServer(RequestProxyDataPacket.TYPE, RequestProxyDataPacket.STREAM_CODEC, RequestProxyDataPacket::handle);
        main.playToClient(ProxyDataResponsePacket.TYPE, ProxyDataResponsePacket.STREAM_CODEC, ProxyDataResponsePacket::handle);
    }
}
