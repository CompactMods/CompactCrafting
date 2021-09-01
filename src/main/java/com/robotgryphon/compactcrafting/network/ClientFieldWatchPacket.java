package com.robotgryphon.compactcrafting.network;

import java.util.function.Supplier;
import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientFieldWatchPacket {

    private final IMiniaturizationField field;

    public ClientFieldWatchPacket(IMiniaturizationField field) {
        this.field = field;
    }

    public ClientFieldWatchPacket(PacketBuffer buf) {
        this.field = new MiniaturizationField();
        field.loadClientData(buf.readAnySizeNbt());
    }

    public static void encode(ClientFieldWatchPacket pkt, PacketBuffer buf) {
        buf.writeNbt(pkt.field.clientData());
    }

    public static boolean handle(ClientFieldWatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.handleFieldData(pkt.field));
        return true;
    }
}
