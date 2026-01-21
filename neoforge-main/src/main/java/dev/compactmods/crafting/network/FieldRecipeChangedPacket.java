package dev.compactmods.crafting.network;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Optional;

public record FieldRecipeChangedPacket(MiniaturizationFieldLocation location, Optional<RecipeHolder<MiniaturizationRecipe>> recipe) implements CustomPacketPayload {

    public static final Type<FieldRecipeChangedPacket> TYPE = new Type<>(CompactCrafting.identifier("field_recipe_changed"));

    static final StreamCodec<RegistryFriendlyByteBuf, FieldRecipeChangedPacket> STREAM_CODEC = StreamCodec.composite(
            MiniaturizationFieldLocation.STREAM_CODEC, FieldRecipeChangedPacket::location,
            ByteBufCodecs.optional(MiniaturizationRecipe.MINI_RECIPE_HOLDER_STREAM_CODEC), FieldRecipeChangedPacket::recipe,
            FieldRecipeChangedPacket::new
    );

    public static final IPayloadHandler<FieldRecipeChangedPacket> HANDLER = (pkt, ctx) -> {
        pkt.recipe.ifPresentOrElse(newRecipe -> {
            ClientPacketHandler.changeFieldRecipe(pkt.location, newRecipe);
        }, () -> ClientPacketHandler.clearFieldRecipe(pkt.location));
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
