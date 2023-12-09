package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.util.FastColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ModConfigSpec CONFIG;

    private static ModConfigSpec.ConfigValue<String> PROJECTOR_COLOR;
    private static ModConfigSpec.ConfigValue<String> PROJECTOR_OFF_COLOR;
    private static ModConfigSpec.IntValue PLACEMENT_TIME;

    public static ModConfigSpec.BooleanValue ENABLE_DEBUG_ON_F3;

    public static int projectorColor = FastColor.ARGB32.color(255, 255, 106, 0);
    public static int projectorOffColor = FastColor.ARGB32.color(255, 137, 137, 137);
    public static int placementTime = 160;

    static {
        generateConfig();
    }

    public static boolean doDebugRender() {
        return ClientUtilities.isDebugScreenOpen() && ENABLE_DEBUG_ON_F3.get();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
    public static void onLoad(final ModConfigEvent.Reloading configEvent) {
        final var c = configEvent.getConfig();
        if(c.getModId().equals(CompactCrafting.MOD_ID) && c.getType().equals(ModConfig.Type.CLIENT)) {
            projectorColor = extractHexColor(PROJECTOR_COLOR.get(), 0x00FF6A00);
            projectorOffColor = extractHexColor(PROJECTOR_OFF_COLOR.get(), 0x00898989);
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
            CompactCrafting.LOGGER.warn("Bad config value for projector color: {}", hex);
            return def;
        }
    }
}
