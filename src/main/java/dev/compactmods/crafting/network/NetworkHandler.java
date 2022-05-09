package dev.compactmods.crafting.network;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompactCrafting.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initialize() {
        MAIN_CHANNEL.messageBuilder(FieldActivatedPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldActivatedPacket::encode)
                .decoder(FieldActivatedPacket::new)
                .consumer(FieldActivatedPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(FieldDeactivatedPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldDeactivatedPacket::encode)
                .decoder(FieldDeactivatedPacket::new)
                .consumer(FieldDeactivatedPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(ClientFieldWatchPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientFieldWatchPacket::encode)
                .decoder(b -> b.readWithCodec(ClientFieldWatchPacket.CODEC))
                .consumer(ClientFieldWatchPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(ClientFieldUnwatchPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientFieldUnwatchPacket::encode)
                .decoder(ClientFieldUnwatchPacket::new)
                .consumer(ClientFieldUnwatchPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(FieldRecipeChangedPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FieldRecipeChangedPacket::encode)
                .decoder(FieldRecipeChangedPacket::new)
                .consumer(FieldRecipeChangedPacket::handle)
                .add();

        MAIN_CHANNEL.messageBuilder(BindToPlatePacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BindToPlatePacket::encode)
                .decoder(BindToPlatePacket::decode)
                .consumer(BindToPlatePacket::handle)
                .add();
    }
}
