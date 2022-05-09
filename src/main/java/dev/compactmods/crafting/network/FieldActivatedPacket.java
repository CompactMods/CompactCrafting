package dev.compactmods.crafting.network;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.MiniaturizationField;
import dev.compactmods.crafting.api.field.FieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class FieldActivatedPacket {

    private final BlockPos center;
    private final FieldSize size;

    @Nullable
    protected CompoundTag clientData;

    protected static final Codec<FieldActivatedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(FieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.size),

            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.center),

            CompoundTag.CODEC.fieldOf("clientData").forGetter(x -> x.clientData)
    ).apply(i, FieldActivatedPacket::new));

    public FieldActivatedPacket(MiniaturizationField field) {
        this.size = field.getFieldSize();
        this.center = field.getCenter();
        this.clientData = field.clientData();
    }

    private FieldActivatedPacket(FieldSize fieldSize, BlockPos center, @Nonnull CompoundTag clientData) {
        this.size = fieldSize;
        this.center = center;
        this.clientData = clientData;
    }

    public FieldActivatedPacket(FriendlyByteBuf buf) {
        FieldActivatedPacket base = buf.readWithCodec(CODEC);
        this.center = base.center;
        this.size = base.size;
        this.clientData = base.clientData;
    }

    public static void handle(FieldActivatedPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientPacketHandler.fieldActivatedNearby(message.size, message.center, message.clientData);
        }));

        ctx.setPacketHandled(true);
    }

    public static void encode(FieldActivatedPacket pkt, FriendlyByteBuf buf) {
        buf.writeWithCodec(CODEC, pkt);
    }
}
