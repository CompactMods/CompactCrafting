package dev.compactmods.crafting.server;

import net.minecraftforge.common.ForgeConfigSpec;

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

        CONFIG = builder.build();
    }
}
