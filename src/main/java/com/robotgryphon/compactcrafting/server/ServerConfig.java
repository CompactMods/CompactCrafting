package com.robotgryphon.compactcrafting.server;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue RECIPE_REGISTRATION;
    public static ForgeConfigSpec.BooleanValue FIELD_BLOCK_CHANGES;
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
