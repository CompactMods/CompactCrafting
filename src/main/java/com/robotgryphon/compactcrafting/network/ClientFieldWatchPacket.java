package com.robotgryphon.compactcrafting.network;

import java.util.function.Supplier;
import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientFieldWatchPacket {

    private final IMiniaturizationField field;
    private final CompoundNBT clientData;

    public ClientFieldWatchPacket(IMiniaturizationField field) {
        this.field = field;
        this.clientData = field.clientData();
    }

    public ClientFieldWatchPacket(PacketBuffer buf) {
        this.field = new MiniaturizationField();
        this.clientData = buf.readAnySizeNbt();
    }

    public static void encode(ClientFieldWatchPacket pkt, PacketBuffer buf) {
        buf.writeNbt(pkt.field.clientData());
    }

    public static boolean handle(ClientFieldWatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.handleFieldData(pkt.clientData));
        return true;
    }
}
