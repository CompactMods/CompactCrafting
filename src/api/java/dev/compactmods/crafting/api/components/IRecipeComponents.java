package dev.compactmods.crafting.api.components;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;

public interface IRecipeComponents {

    Map<String, IRecipeComponent> getAllComponents();

    Map<String, IRecipeBlockComponent> getBlockComponents();

    boolean isEmptyBlock(String key);

    Optional<IRecipeBlockComponent> getBlock(String key);

    boolean hasBlock(String key);

    void registerBlock(String key, IRecipeBlockComponent component);
    void unregisterBlock(String key);

    void registerOther(String key, IRecipeComponent component);

    int size();

    void clear();

    Optional<String> getKey(BlockState state);

    Stream<String> getEmptyComponents();
}
