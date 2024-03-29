package dev.compactmods.crafting.network;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class FieldRecipeChangedPacket {

    private final BlockPos fieldCenter;

    @Nullable
    private final ResourceLocation recipe;

    public FieldRecipeChangedPacket(IMiniaturizationField field) {
        this.fieldCenter = field.getCenter();
        this.recipe = field.getCurrentRecipe().map(IMiniaturizationRecipe::getRecipeIdentifier).orElse(null);
    }

    public FieldRecipeChangedPacket(FriendlyByteBuf buf) {
        this.fieldCenter = buf.readBlockPos();
        if(buf.readBoolean())
            this.recipe = ResourceLocation.tryParse(buf.readUtf());
        else
            this.recipe = null;
    }

    public static void encode(FieldRecipeChangedPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.fieldCenter);
        buf.writeBoolean(pkt.recipe != null);
        if(pkt.recipe != null)
            buf.writeUtf(pkt.recipe.toString());
    }

    public static boolean handle(FieldRecipeChangedPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.handleRecipeChanged(pkt.fieldCenter, pkt.recipe));
        return true;
    }
}
