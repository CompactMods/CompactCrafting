package dev.compactmods.crafting.client;

import com.mojang.serialization.JavaOps;
import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.util.ARGB;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT)
public class ClientConfig {

    public static ModConfigSpec CONFIG;

    private static ModConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    private static ModConfigSpec.ConfigValue<String> PROJECTOR_OFF_COLOR;
    private static ModConfigSpec.IntValue PLACEMENT_TIME;

    public static int projectorColor = ARGB.color(255, 255, 106, 0);
    public static int projectorOffColor = ARGB.color(255, 137, 137, 137);
    public static int placementTime = 160;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder
                .comment("Projector Settings")
                .push("projectors");

        PROJECTOR_COLOR = builder
                .comment(
                        "The fieldBaseColor for the projector fields. (HEX format)",
                        "Examples: Orange - #FF6A00, Violet - #32174D, Green - #00A658, Blue - #3A7FE1"
                )
                .define("projectorColor", "#FF6A00");

        PROJECTOR_OFF_COLOR = builder
                .comment("The fieldBaseColor for the projectors when not active. (HEX format)")
                .define("projectorOffColor", "#898989");

        PLACEMENT_TIME = builder
                .comment("How long (ticks) the projectorInfo helper will show on right-clicking a projector.")
                .defineInRange("placementTime", 160, 60, 240);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Reloading configEvent) {
        final var c = configEvent.getConfig();
        if(c.getModId().equals(CompactCrafting.MOD_ID) && c.getType().equals(ModConfig.Type.CLIENT)) {
            var onColor = PROJECTOR_COLOR.get();

            int test;
            switch (onColor.length()) {
                case 6 -> test = ExtraCodecs.STRING_RGB_COLOR
                        .parse(JavaOps.INSTANCE, onColor)
                        .result()
                        .orElse(0);
                case 8 -> test = ExtraCodecs.STRING_ARGB_COLOR
                        .parse(JavaOps.INSTANCE, onColor)
                        .result()
                        .orElse(0);
            }

            projectorColor = extractHexColor(onColor, 0xFFFF6A00);
            projectorOffColor = extractHexColor(PROJECTOR_OFF_COLOR.get(), 0xFF898989);
            placementTime = PLACEMENT_TIME.get();
        }
    }

    private static int extractHexColor(String hex, int def) {
        try {
            if (hex.startsWith("#"))
                return Integer.parseInt(hex.substring(1), 16);
            else
                return def;
        } catch (NumberFormatException nfe) {
            CompactCrafting.LOGGER.warn("Bad config value for projector fieldBaseColor: {}", hex);
            return def;
        }
    }
}
