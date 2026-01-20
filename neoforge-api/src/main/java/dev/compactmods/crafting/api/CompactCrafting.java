package dev.compactmods.crafting.api;

import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CompactCrafting {
    public static final Logger LOGGER = LogManager.getLogger(CompactCrafting.MOD_ID);
    public static final Logger RECIPE_LOGGER = LogManager.getLogger("CCRecipeLoader");

    public static final String MOD_ID = "compactcrafting";

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String identifierString(String path) {
        return MOD_ID + ":" + path;
    }
}
