package dev.compactmods.crafting.network;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class FieldRecipeChangedPacket {

    private final BlockPos fieldCenter;

    @Nullable
    private final ResourceLocation recipe;

    public FieldRecipeChangedPacket(IMiniaturizationField field) {
        this.fieldCenter = field.getCenter();
        this.recipe = field.getCurrentRecipe().map(IMiniaturizationRecipe::getRecipeIdentifier).orElse(null);
    }

    public FieldRecipeChangedPacket(PacketBuffer buf) {
        this.fieldCenter = buf.readBlockPos();
        if(buf.readBoolean())
            this.recipe = ResourceLocation.tryParse(buf.readUtf());
        else
            this.recipe = null;
    }

    public static void encode(FieldRecipeChangedPacket pkt, PacketBuffer buf) {
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
