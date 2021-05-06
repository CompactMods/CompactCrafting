package com.robotgryphon.compactcrafting.network;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {
    private static int index = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompactCrafting.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initialize() {
        MAIN_CHANNEL.registerMessage(index++, FieldActivatedPacket.class,
                FieldActivatedPacket::encode, FieldActivatedPacket::decode,
                FieldActivatedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        MAIN_CHANNEL.registerMessage(index++, FieldDeactivatedPacket.class,
                FieldDeactivatedPacket::encode, FieldDeactivatedPacket::decode,
                FieldDeactivatedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        MAIN_CHANNEL.registerMessage(index++, PlayMiniaturizationSoundPacket.class,
                PlayMiniaturizationSoundPacket::encode, PlayMiniaturizationSoundPacket::decode,
                PlayMiniaturizationSoundPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
