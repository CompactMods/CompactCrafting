package com.robotgryphon.compactcrafting.network;

import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayMiniaturizationSoundPacket {
    private final BlockPos fieldPreviewPos;

    public PlayMiniaturizationSoundPacket(BlockPos fieldPreviewPos) {
        this.fieldPreviewPos = fieldPreviewPos;
    }

    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(this.fieldPreviewPos);
    }

    public static PlayMiniaturizationSoundPacket decode(PacketBuffer buf) {
        return new PlayMiniaturizationSoundPacket(buf.readBlockPos());
    }

    public static void handle(PlayMiniaturizationSoundPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientPacketHandler.handlePlayMiniaturizationSound(message.fieldPreviewPos)));

        ctx.setPacketHandled(true);
    }
}
