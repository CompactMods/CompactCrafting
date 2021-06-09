package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    public static int projectorColor = 0xFFFFFFFF;

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
        String tempColor = PROJECTOR_COLOR.get();
        int finalColor = 0xFFFFFFFF;
        try {
            if(tempColor.startsWith("#"))
                finalColor = Integer.parseInt(tempColor.substring(1), 16);
            else
                finalColor = 0x00FF6A00;
        }

        catch(NumberFormatException nfe) {
            CompactCrafting.LOGGER.warn("Bad config value for projector color: {}", tempColor);
            finalColor = 0x00FF6A00;
        }

        projectorColor = finalColor;
    }
}
