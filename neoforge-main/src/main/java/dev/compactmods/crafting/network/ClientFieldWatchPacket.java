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

public record ClientFieldWatchPacket(MiniaturizationFieldSize fieldSize, BlockPos center, CompoundTag clientData) implements CustomPacketPayload {

    public static final Type<ClientFieldWatchPacket> TYPE = new Type<>(CompactCrafting.modRL("field_watch"));

    public ClientFieldWatchPacket(IMiniaturizationField<MiniaturizationRecipe> field) {
        this(field.getFieldSize(), field.getCenter(), getClientData(field));
    }
    
    private static CompoundTag getClientData(IMiniaturizationField<MiniaturizationRecipe> field) {
        if (field instanceof MiniaturizationField mf) {
            return mf.serverData();
        }
        return new CompoundTag();
    }

    public static final StreamCodec<FriendlyByteBuf, ClientFieldWatchPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldSize.STREAM_CODEC, ClientFieldWatchPacket::fieldSize,
            BlockPos.STREAM_CODEC, ClientFieldWatchPacket::center,
            ByteBufCodecs.COMPOUND_TAG, ClientFieldWatchPacket::clientData,
            ClientFieldWatchPacket::new
    );

    public static final IPayloadHandler<ClientFieldWatchPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientPacketHandler.handleFieldData(pkt.clientData);
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
