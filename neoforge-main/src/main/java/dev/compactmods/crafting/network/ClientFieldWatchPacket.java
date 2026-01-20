//package dev.compactmods.crafting.network;
//
//import dev.compactmods.crafting.api.projectors.IMiniaturizationField;
//import dev.compactmods.crafting.client.ClientPacketHandler;
//import dev.compactmods.crafting.projectors.MiniaturizationField;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//
//public class ClientFieldWatchPacket {
//
//    private final IMiniaturizationField projectors;
//    private final CompoundTag clientData;
//
//    public ClientFieldWatchPacket(IMiniaturizationField projectors) {
//        this.projectors = projectors;
//        this.clientData = projectors.clientData();
//    }
//
//    public ClientFieldWatchPacket(FriendlyByteBuf buf) {
//        this.projectors = new MiniaturizationField();
//        this.clientData = buf.readNbt();
//    }
//
//    public static void encode(ClientFieldWatchPacket pkt, FriendlyByteBuf buf) {
//        buf.writeNbt(pkt.projectors.clientData());
//    }
//
//    public static void handle(ClientFieldWatchPacket pkt) {
//        ClientPacketHandler.handleFieldData(pkt.clientData);
//    }
//}
