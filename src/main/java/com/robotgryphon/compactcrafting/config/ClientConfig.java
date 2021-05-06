package com.robotgryphon.compactcrafting.config;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static final ForgeConfigSpec CONFIG;

    private static final ForgeConfigSpec.ConfigValue<String> PROJECTOR_COLOR_SPEC;
    private static final ForgeConfigSpec.ConfigValue<String> PROJECTOR_DONE_COLOR_SPEC;
    private static final ForgeConfigSpec.ConfigValue<Boolean> USE_DONE_COLOR_SPEC;

    private static Color projectorColor = Color.white;
    private static Color projectorDoneColor = Color.white;
    private static boolean useDoneColor = true;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Projector Settings")
                .push("projectors");

        PROJECTOR_COLOR_SPEC = builder
                .comment(
                        getHexComment("The default color for a projector field when no recipe can be matched.")
                )
                .define("projectorColor", "#FF6A00");

        PROJECTOR_DONE_COLOR_SPEC = builder
                .comment(
                        getHexComment("The color for a projector field when a recipe has finished crafting.")
                )
                .define("projectorDoneColor", "#00A658");

        USE_DONE_COLOR_SPEC = builder
                .comment("Whether to enable the done color for a projector field, otherwise always use the unmatched color.")
                .define("enableProjectorDoneColor", true);

        builder.pop();

        CONFIG = builder.build();
    }

    public static Color getProjectorColor() {
        return projectorColor;
    }

    public static Color getProjectorDoneColor() {
        return useDoneColor ? projectorDoneColor : projectorColor;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.ModConfigEvent configEvent) {
        projectorColor = Color.decode(PROJECTOR_COLOR_SPEC.get());
        projectorDoneColor = Color.decode(PROJECTOR_DONE_COLOR_SPEC.get());
        useDoneColor = USE_DONE_COLOR_SPEC.get();
    }

    private static String[] getHexComment(String base) {
        return new String[]{
                base + " (HEX color format)",
                "Examples: Orange - #FF6A00, Violet - #32174D, Green - #00A658, Blue - #3A7FE1"
        };
    }
}
