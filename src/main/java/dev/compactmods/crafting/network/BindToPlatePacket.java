package dev.compactmods.crafting.network;

import dev.compactmods.crafting.core.CCCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BindToPlatePacket(ResourceLocation recipeId) {

    public static void encode(BindToPlatePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.recipeId.toString());
    }

    public static BindToPlatePacket decode(FriendlyByteBuf buf) {
        final var id = new ResourceLocation(buf.readUtf());
        return new BindToPlatePacket(id);
    }

    public static boolean handle(BindToPlatePacket pkt, Supplier<NetworkEvent.Context> context) {
        final var ctx = context.get();
        final var sender = ctx.getSender();
        if(sender == null) return true;

        sender.level.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
            fields.getFields(sender.chunkPosition())
                    .filter(field -> field.getBounds().contains(sender.position()))
                    .forEach(field -> {
                        // todo field.setDedicatedRecipe(pkt.recipeId);
                    });
        });

        return true;
    }
}
