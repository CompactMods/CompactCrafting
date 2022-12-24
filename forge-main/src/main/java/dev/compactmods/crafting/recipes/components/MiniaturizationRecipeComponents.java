package dev.compactmods.crafting.recipes.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import net.minecraft.world.level.block.state.BlockState;

public class MiniaturizationRecipeComponents implements IRecipeComponents {

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private final Map<String, IRecipeBlockComponent> blockComponents;

    /**
     * Contains a mapping of non-block components in the recipe.
     * To be used for future expansion.
     */
    private final Map<String, IRecipeComponent> otherComponents;

    public MiniaturizationRecipeComponents() {
        this.blockComponents = new HashMap<>();
        this.otherComponents = new HashMap<>();
    }

    @Override
    public Map<String, IRecipeComponent> getAllComponents() {
        return Stream.concat(
                blockComponents.entrySet().stream(),
                otherComponents.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, IRecipeBlockComponent> getBlockComponents() {
        return Collections.unmodifiableMap(blockComponents);
    }

    @Override
    public boolean isEmptyBlock(String key) {
        IRecipeBlockComponent comp = blockComponents.get(key);
        if (comp == null)
            return true;

        return comp instanceof EmptyBlockComponent;
    }

    @Override
    public boolean hasBlock(String key) {
        return blockComponents.containsKey(key);
    }

    @Override
    public void registerBlock(String key, IRecipeBlockComponent component) {
        blockComponents.put(key, component);
    }

    @Override
    public void unregisterBlock(String key) {
        blockComponents.remove(key);
    }

    @Override
    public void registerOther(String key, IRecipeComponent component) {
        otherComponents.put(key, component);
    }

    @Override
    public int size() {
        return this.otherComponents.size() + this.blockComponents.size();
    }

    @Override
    public void clear() {
        this.otherComponents.clear();
        this.blockComponents.clear();
    }

    public Optional<IRecipeBlockComponent> getBlock(String key) {
        if (this.blockComponents.containsKey(key)) {
            IRecipeBlockComponent component = blockComponents.get(key);
            return Optional.of(component);
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getKey(BlockState state) {
        return blockComponents.entrySet()
                .stream()
                .filter(bs -> bs.getValue().matches(state))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    @Override
    public Stream<String> getEmptyComponents() {
        return blockComponents.keySet()
                .stream()
                .filter(bck -> blockComponents.get(bck) instanceof EmptyBlockComponent);
    }
}
