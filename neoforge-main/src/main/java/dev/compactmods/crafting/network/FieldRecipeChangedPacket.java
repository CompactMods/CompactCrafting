package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

public record FieldRecipeChangedPacket(BlockPos fieldCenter, Optional<RecipeHolder<MiniaturizationRecipe>> recipe) implements CustomPacketPayload {

    public static final Type<FieldRecipeChangedPacket> TYPE = new Type<>(CompactCrafting.modRL("field_recipe_changed"));

    static final StreamCodec<RegistryFriendlyByteBuf, FieldRecipeChangedPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FieldRecipeChangedPacket::fieldCenter,
            ByteBufCodecs.optional(MiniaturizationRecipe.MINI_RECIPE_HOLDER_STREAM_CODEC), FieldRecipeChangedPacket::recipe,
            FieldRecipeChangedPacket::new
    );

    public static final IPayloadHandler<FieldRecipeChangedPacket> HANDLER = (pkt, ctx) -> {
        pkt.recipe.ifPresentOrElse(newRecipe -> {
            ClientPacketHandler.changeFieldRecipe(pkt.fieldCenter, newRecipe);
        }, () -> ClientPacketHandler.clearFieldRecipe(pkt.fieldCenter));
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
