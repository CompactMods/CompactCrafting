package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

public record FieldDeactivatedPacket(MiniaturizationFieldSize fieldSize, BlockPos fieldCenter, List<BlockPos> projectors) implements CustomPacketPayload {

    public static final Type<FieldDeactivatedPacket> TYPE = new Type<>(CompactCrafting.modRL("field_deactivated"));

    static final StreamCodec<FriendlyByteBuf, FieldDeactivatedPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldSize.STREAM_CODEC, FieldDeactivatedPacket::fieldSize,
            BlockPos.STREAM_CODEC, FieldDeactivatedPacket::fieldCenter,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), FieldDeactivatedPacket::projectors,
            FieldDeactivatedPacket::new
    );

    public static final IPayloadHandler<FieldDeactivatedPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.getDist().isClient()) {
                ClientPacketHandler.handleFieldDeactivation(pkt.fieldCenter);
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
