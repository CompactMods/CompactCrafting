package dev.compactmods.crafting.server;

import com.electronwill.nightconfig.core.EnumGetMethod;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.FieldDestabilizeHandling;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {

    public static ForgeConfigSpec CONFIG;

    /**
     * Enabled if the user wants debug logging for the recipe registration process.
     */
    public static ForgeConfigSpec.BooleanValue RECIPE_REGISTRATION;

    /**
     * Enabled if the user wants more logging for when blocks change near fields.
     */
    public static ForgeConfigSpec.BooleanValue FIELD_BLOCK_CHANGES;

    /**
     * Enabled if the user requests debug logging for the recipe matching process.
     */
    public static ForgeConfigSpec.BooleanValue RECIPE_MATCHING;

    private static ForgeConfigSpec.EnumValue<FieldDestabilizeHandling> FIELD_DESTABILIZE_HANDLING;
    public static FieldDestabilizeHandling DESTABILIZE_HANDLING = FieldDestabilizeHandling.RESTORE_ALL;
    
    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Logging Settings")
                .push("logging");

        RECIPE_REGISTRATION = builder
                .comment("Enables more logging during recipe registration and syncing.")
                .define("recipeRegistration", false);

        FIELD_BLOCK_CHANGES = builder
                .comment("Enables more logging for fields handling nearby block updates.")
                .define("fieldBlockChanges", false);

        RECIPE_MATCHING = builder
                .comment("Enables more logging for the recipe matching process.")
                .define("recipeMatching", false);

        builder.pop();

        builder.comment("Field Settings").push("field");

        FIELD_DESTABILIZE_HANDLING = builder
                .comment("Changes how the field handles a destabilization event (such as a projector breaking mid-craft)")
                .defineEnum("destabilizeHandling", FieldDestabilizeHandling.RESTORE_ALL, EnumGetMethod.NAME_IGNORECASE);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onConfigEvent(final ModConfigEvent.Reloading configEvent) {
        ServerConfig.DESTABILIZE_HANDLING = ServerConfig.FIELD_DESTABILIZE_HANDLING.get();
    }
}
