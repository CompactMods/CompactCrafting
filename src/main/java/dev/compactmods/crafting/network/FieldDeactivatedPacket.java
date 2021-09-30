package dev.compactmods.crafting.network;

import java.io.IOException;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class FieldDeactivatedPacket {

    protected static final Codec<FieldDeactivatedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(MiniaturizationFieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.fieldSize),
            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.fieldCenter)
    ).apply(i, FieldDeactivatedPacket::new));

    private final MiniaturizationFieldSize fieldSize;
    private final BlockPos fieldCenter;
    private final BlockPos[] projectors;

    public FieldDeactivatedPacket(MiniaturizationFieldSize fieldSize, BlockPos fieldCenter) {
        this.fieldSize = fieldSize;
        this.fieldCenter = fieldCenter;

        this.projectors = fieldSize.getProjectorLocations(fieldCenter)
                .map(BlockPos::immutable).toArray(BlockPos[]::new);
    }

    public FieldDeactivatedPacket(PacketBuffer buf) {
        FieldDeactivatedPacket pkt = null;
        try {
            pkt = buf.readWithCodec(CODEC);
        } catch (IOException e) {
            CompactCrafting.LOGGER.error(e);
        }

        if(pkt != null) {
            this.fieldSize = pkt.fieldSize;
            this.fieldCenter = pkt.fieldCenter;

            this.projectors = fieldSize.getProjectorLocations(fieldCenter)
                    .map(BlockPos::immutable).toArray(BlockPos[]::new);
        } else {
            this.fieldSize = MiniaturizationFieldSize.INACTIVE;
            this.fieldCenter = BlockPos.ZERO;
            this.projectors = new BlockPos[0];
        }
    }

    public static boolean handle(FieldDeactivatedPacket message, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPacketHandler.handleFieldDeactivation(message.projectors);
        });

        return true;
    }

    public static void encode(FieldDeactivatedPacket pkt, PacketBuffer buf) {
        try {
            buf.writeWithCodec(CODEC, pkt);
        } catch (IOException e) {
            CompactCrafting.LOGGER.error(e);
        }
    }
}
