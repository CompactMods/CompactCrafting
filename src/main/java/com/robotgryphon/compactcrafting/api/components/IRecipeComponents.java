package com.robotgryphon.compactcrafting.api.components;

import java.util.Map;
import java.util.Optional;

public interface IRecipeComponents {

    Map<String, IRecipeComponent> getAllComponents();

    Map<String, IRecipeBlockComponent> getBlockComponents();

    boolean isEmptyBlock(String key);

    Optional<IRecipeBlockComponent> getBlock(String key);

    boolean hasBlock(String key);

    void registerBlock(String key, IRecipeBlockComponent component);

    int size();
}
