package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record FieldActivatedPacket(IMiniaturizationField<MiniaturizationRecipe> field, CompoundTag clientData) implements CustomPacketPayload {

    public static final Type<FieldActivatedPacket> TYPE = new Type<>(CompactCrafting.modRL("field_activated"));

    static final StreamCodec<FriendlyByteBuf, FieldActivatedPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldSize.STREAM_CODEC, pkt -> pkt.field.getFieldSize(),
            BlockPos.STREAM_CODEC, pkt -> pkt.field.getCenter(),
            ByteBufCodecs.COMPOUND_TAG, FieldActivatedPacket::clientData,
            FieldActivatedPacket::client
    );

    private static FieldActivatedPacket client(MiniaturizationFieldSize fieldSize, BlockPos center, CompoundTag clientData) {
        return ClientPacketHandler.createFieldActivationPacket(fieldSize, center, clientData);
    }

    public static final IPayloadHandler<FieldActivatedPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandler.handleFieldActivation(pkt.field, pkt.clientData);
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
