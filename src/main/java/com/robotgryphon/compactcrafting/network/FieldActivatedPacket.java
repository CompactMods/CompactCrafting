package com.robotgryphon.compactcrafting.network;

import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FieldActivatedPacket {
    private BlockPos position;
    private FieldProjectionSize fieldSize;

    private FieldActivatedPacket() {
    }

    public FieldActivatedPacket(BlockPos center, FieldProjectionSize fieldSize) {
        this.position = center;
        this.fieldSize = fieldSize;
    }

    public static void handle(FieldActivatedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handleFieldActivation(message.position, message.fieldSize);
                return null;
            });
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(FieldActivatedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.position);
        buf.writeUtf(pkt.fieldSize.name());
    }

    public static FieldActivatedPacket decode(PacketBuffer buf) {
        FieldActivatedPacket pkt = new FieldActivatedPacket();
        pkt.position = buf.readBlockPos();
        pkt.fieldSize = FieldProjectionSize.valueOf(buf.readUtf());

        return pkt;
    }
}
