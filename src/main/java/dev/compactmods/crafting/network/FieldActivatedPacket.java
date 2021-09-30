package dev.compactmods.crafting.network;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.field.MiniaturizationField;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class FieldActivatedPacket {

    private IMiniaturizationField field;

    @Nullable
    protected CompoundNBT clientData;

    protected static final Codec<FieldActivatedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(MiniaturizationFieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.field.getFieldSize()),

            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.field.getCenter()),

            CompoundNBT.CODEC.fieldOf("clientData").forGetter(x -> x.clientData)
    ).apply(i, FieldActivatedPacket::new));

    public FieldActivatedPacket(IMiniaturizationField field) {
        this.field = field;
        this.clientData = field.clientData();
    }

    private FieldActivatedPacket(MiniaturizationFieldSize fieldSize, BlockPos center, CompoundNBT clientData) {
        this.field = new MiniaturizationField();
        field.setSize(fieldSize);
        field.setCenter(center);
        this.clientData = clientData;
    }

    public FieldActivatedPacket(PacketBuffer buf) {
        try {
            FieldActivatedPacket base = buf.readWithCodec(CODEC);
            this.field = base.field;
            this.clientData = base.clientData;
        } catch (IOException e) {
            CompactCrafting.LOGGER.error(e);
        }
    }

    public static void handle(FieldActivatedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPacketHandler.handleFieldActivation(message.field, message.clientData);
        }));

        ctx.setPacketHandled(true);
    }

    public static void encode(FieldActivatedPacket pkt, PacketBuffer buf) {
        try {
            buf.writeWithCodec(CODEC, pkt);
        } catch (IOException e) {
            CompactCrafting.LOGGER.error(e);
        }
    }
}
