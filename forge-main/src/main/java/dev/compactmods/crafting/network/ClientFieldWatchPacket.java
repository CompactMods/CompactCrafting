package dev.compactmods.crafting.network;

import java.util.function.Supplier;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.field.MiniaturizationField;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ClientFieldWatchPacket {

    private final IMiniaturizationField field;
    private final CompoundTag clientData;

    public ClientFieldWatchPacket(IMiniaturizationField field) {
        this.field = field;
        this.clientData = field.clientData();
    }

    public ClientFieldWatchPacket(FriendlyByteBuf buf) {
        this.field = new MiniaturizationField();
        this.clientData = buf.readAnySizeNbt();
    }

    public static void encode(ClientFieldWatchPacket pkt, FriendlyByteBuf buf) {
        buf.writeNbt(pkt.field.clientData());
    }

    public static boolean handle(ClientFieldWatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        ClientPacketHandler.handleFieldData(pkt.clientData);
        return true;
    }
}
