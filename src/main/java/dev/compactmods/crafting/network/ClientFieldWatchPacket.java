package dev.compactmods.crafting.network;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.FieldSize;
import dev.compactmods.crafting.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record ClientFieldWatchPacket(FieldSize size, BlockPos center, CompoundTag clientData) {

    static final Codec<ClientFieldWatchPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(FieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.size),

            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.center),

            CompoundTag.CODEC.fieldOf("clientData").forGetter(x -> x.clientData)
    ).apply(i, ClientFieldWatchPacket::new));

    public static void encode(ClientFieldWatchPacket pkt, FriendlyByteBuf buf) {
        buf.writeNbt(pkt.clientData());
    }

    public static boolean handle(ClientFieldWatchPacket pkt, Supplier<NetworkEvent.Context> context) {
        ClientPacketHandler.fieldBeganWatching(pkt.size, pkt.center, pkt.clientData);
        return true;
    }
}
