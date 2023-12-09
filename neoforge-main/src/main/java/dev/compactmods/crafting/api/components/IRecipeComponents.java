package dev.compactmods.crafting.api.components;

import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface IRecipeComponents {

    Map<String, dev.compactmods.crafting.api.components.IRecipeComponent> getAllComponents();

    Map<String, dev.compactmods.crafting.api.components.IRecipeBlockComponent> getBlockComponents();

    boolean isEmptyBlock(String key);

    Optional<dev.compactmods.crafting.api.components.IRecipeBlockComponent> getBlock(String key);

    boolean hasBlock(String key);

    void registerBlock(String key, IRecipeBlockComponent component);
    void unregisterBlock(String key);

    void registerOther(String key, IRecipeComponent component);

    int size();

    void clear();

    Optional<String> getKey(BlockState state);

    Stream<String> getEmptyComponents();

    boolean isKnownKey(String key);
}
