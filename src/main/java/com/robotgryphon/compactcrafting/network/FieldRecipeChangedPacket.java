package com.robotgryphon.compactcrafting.network;

import java.util.function.Supplier;
import com.robotgryphon.compactcrafting.client.ClientPacketHandler;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class FieldRecipeChangedPacket {

    private final BlockPos fieldCenter;
    private final ResourceLocation recipe;

    public FieldRecipeChangedPacket(IMiniaturizationField field) {
        this.fieldCenter = field.getCenter();
        this.recipe = field.getCurrentRecipe().map(IMiniaturizationRecipe::getId).orElse(null);
    }

    public FieldRecipeChangedPacket(PacketBuffer buf) {
        this.fieldCenter = buf.readBlockPos();
        this.recipe = ResourceLocation.tryParse(buf.readUtf());
    }

    public static void encode(FieldRecipeChangedPacket pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.fieldCenter);
        buf.writeUtf(pkt.recipe.toString());
    }

    public static boolean handle(FieldRecipeChangedPacket pkt, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientPacketHandler.handleRecipeChanged(pkt.fieldCenter, pkt.recipe));
        return true;
    }
}
