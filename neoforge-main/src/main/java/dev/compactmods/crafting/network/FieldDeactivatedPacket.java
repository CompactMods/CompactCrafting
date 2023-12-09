package dev.compactmods.crafting.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public FieldDeactivatedPacket(FriendlyByteBuf buf) {
        FieldDeactivatedPacket pkt = buf.readJsonWithCodec(CODEC);

        this.fieldSize = pkt.fieldSize;
        this.fieldCenter = pkt.fieldCenter;

        this.projectors = fieldSize.getProjectorLocations(fieldCenter)
                .map(BlockPos::immutable).toArray(BlockPos[]::new);
    }

    public static boolean handle(FieldDeactivatedPacket message, NetworkEvent.Context context) {

        if(FMLEnvironment.dist.isClient()) {
            ClientPacketHandler.handleFieldDeactivation(message.fieldCenter);
        }

        return true;
    }

    public static void encode(FieldDeactivatedPacket pkt, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(CODEC, pkt);
    }
}
