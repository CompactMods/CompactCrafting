package com.robotgryphon.compactcrafting.recipes.components;

import com.robotgryphon.compactcrafting.api.components.IRecipeBlockComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CCMiniRecipeComponents implements IRecipeComponents {

    /**
     * Contains a mapping of all known components in the recipe.
     * Vanilla style; C = CHARCOAL_BLOCK
     */
    private Map<String, IRecipeBlockComponent> blockComponents;

    /**
     * Contains a mapping of non-block components in the recipe.
     * To be used for future expansion.
     */
    private Map<String, IRecipeComponent> otherComponents;

    public CCMiniRecipeComponents() {
        this.blockComponents = new HashMap<>();
        this.otherComponents = new HashMap<>();
    }

    public void apply(Map<String, IRecipeComponent> compMap) {
        this.blockComponents = new HashMap<>();
        this.otherComponents = new HashMap<>();
        for (Map.Entry<String, IRecipeComponent> comp : compMap.entrySet()) {
            // Map in block components
            if (comp.getValue() instanceof IRecipeBlockComponent) {
                this.blockComponents.put(comp.getKey(), (IRecipeBlockComponent) comp.getValue());
                continue;
            }

            this.otherComponents.put(comp.getKey(), comp.getValue());
        }
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
        return new HashMap<>(blockComponents);
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
    public int size() {
        return this.otherComponents.size() + this.blockComponents.size();
    }


    public Optional<IRecipeBlockComponent> getBlock(String key) {
        if (this.blockComponents.containsKey(key)) {
            IRecipeBlockComponent component = blockComponents.get(key);
            return Optional.of(component);
        }

        return Optional.empty();
    }
}
