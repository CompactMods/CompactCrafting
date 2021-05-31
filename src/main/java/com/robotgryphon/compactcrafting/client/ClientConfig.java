package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.awt.*;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    public static Color projectorColor = Color.white;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Projector Settings")
                .push("projectors");

        String sep = System.lineSeparator();

        PROJECTOR_COLOR = builder
                .comment(
                        "The color for the projector fields. (HEX format)",
                        "Examples: Orange - #FF6A00, Violet - #32174D, Green - #00A658, Blue - #3A7FE1"
                )
                .define("projectorColor", "#FF6A00");

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.ModConfigEvent configEvent) {
        projectorColor = Color.decode(PROJECTOR_COLOR.get());
    }
}
