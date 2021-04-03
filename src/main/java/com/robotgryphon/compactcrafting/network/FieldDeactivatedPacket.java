package com.robotgryphon.compactcrafting.network;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FieldDeactivatedPacket {
    private BlockPos position;
    private FieldProjectionSize fieldSize;

    private FieldDeactivatedPacket() {
    }

    public FieldDeactivatedPacket(BlockPos center, FieldProjectionSize fieldSize) {
        this.position = center;
        this.fieldSize = fieldSize;
    }

    public static void handle(FieldDeactivatedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handleFieldDeactivation(message.position, message.fieldSize);
                return null;
            });
        });

        ctx.setPacketHandled(true);
    }

    public static void encode(FieldDeactivatedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.position);
        String n = pkt.fieldSize.name();
        CompactCrafting.LOGGER.debug("D: {}, N: {}", pkt.position, n);
        buf.writeUtf(n);
    }

    public static FieldDeactivatedPacket decode(PacketBuffer buf) {
        FieldDeactivatedPacket pkt = new FieldDeactivatedPacket();
        pkt.position = buf.readBlockPos();
        pkt.fieldSize = FieldProjectionSize.valueOf(buf.readUtf());

        return pkt;
    }
}
