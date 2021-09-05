package dev.compactmods.crafting.network;

import java.util.function.Supplier;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClientFieldUnwatchPacket {

    private final BlockPos center;

    public ClientFieldUnwatchPacket(BlockPos center) {
        this.center = center;
    }

    public ClientFieldUnwatchPacket(PacketBuffer buf) {
        this.center = buf.readBlockPos();
    }

    public static void encode(ClientFieldUnwatchPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.center);
    }

    public static boolean handle(ClientFieldUnwatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.removeField(pkt.center));
        return true;
    }
}
