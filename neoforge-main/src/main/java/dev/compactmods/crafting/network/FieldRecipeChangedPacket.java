package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

public record FieldRecipeChangedPacket(BlockPos fieldCenter, Optional<ResourceLocation> recipe) implements CustomPacketPayload {

    public static final Type<FieldRecipeChangedPacket> TYPE = new Type<>(CompactCrafting.modRL("field_recipe_changed"));

    static final StreamCodec<RegistryFriendlyByteBuf, FieldRecipeChangedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FieldRecipeChangedPacket::fieldCenter,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), FieldRecipeChangedPacket::recipe,
            FieldRecipeChangedPacket::new
    );

    public static final IPayloadHandler<FieldRecipeChangedPacket> HANDLER = (pkt, ctx) -> {
        ClientPacketHandler.handleRecipeChanged(pkt.fieldCenter, pkt.recipe.orElse(null));
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
