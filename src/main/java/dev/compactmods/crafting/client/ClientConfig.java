package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    private static ForgeConfigSpec.ConfigValue<String> PROJECTOR_OFF_COLOR;
    private static ForgeConfigSpec.IntValue PLACEMENT_TIME;

    public static ForgeConfigSpec.BooleanValue ENABLE_DEBUG_ON_F3;

    public static int projectorColor = 0xFFFFFFFF;
    public static int projectorOffColor = 0xFFFFFFFF;
    public static int placementTime = 60;

    static {
        generateConfig();
    }

    public static boolean doDebugRender() {
        return Minecraft.getInstance().options.renderDebug && ENABLE_DEBUG_ON_F3.get();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Projector Settings")
                .push("projectors");

        PROJECTOR_COLOR = builder
                .comment(
                        "The color for the projector fields. (HEX format)",
                        "Examples: Orange - #FF6A00, Violet - #32174D, Green - #00A658, Blue - #3A7FE1"
                )
                .define("projectorColor", "#FF6A00");

        PROJECTOR_OFF_COLOR = builder
                .comment("The color for the projectors when not active. (HEX format)")
                .define("projectorOffColor", "#898989");

        ENABLE_DEBUG_ON_F3 = builder
                .comment("Whether or not activating F3 will enable debug renderers.")
                .define("projectorDebugger", false);

        PLACEMENT_TIME = builder
                .comment("How long (ticks) the placement helper will show on right-clicking a projector.")
                .defineInRange("placementTime", 160, 60, 240);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.ModConfigEvent configEvent) {
        projectorColor = extractHexColor(PROJECTOR_COLOR.get(), 0x00FF6A00);
        projectorOffColor = extractHexColor(PROJECTOR_OFF_COLOR.get(), 0x00898989);
        placementTime = PLACEMENT_TIME.get();
    }

    private static int extractHexColor(String hex, int def) {
        try {
            if (hex.startsWith("#"))
                return Integer.parseInt(hex.substring(1), 16);
            else
                return def;
        } catch (NumberFormatException nfe) {
            CompactCrafting.LOGGER.warn("Bad config value for projector color: {}", hex);
            return def;
        }
    }
}
