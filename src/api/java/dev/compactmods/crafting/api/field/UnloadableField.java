package dev.compactmods.crafting.api.field;

public interface UnloadableField {

    boolean isLoaded();

    default void checkLoaded() {
    }
}
