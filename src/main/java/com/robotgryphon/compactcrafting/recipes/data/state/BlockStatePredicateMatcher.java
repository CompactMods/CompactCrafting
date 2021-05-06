package com.robotgryphon.compactcrafting.recipes.data.state;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BlockStatePredicateMatcher {

    public Block block;
    private final Map<String, Predicate<Comparable<?>>> filters;
    private boolean allowAll;

    public BlockStatePredicateMatcher(Block b) {
        this.block = b;
        this.filters = new HashMap<>();
        this.allowAll = true;
    }

    public void setFilter(String property, Predicate<Comparable<?>> val) {
        // Check property exists by name
        Property<?> property1 = block.getStateDefinition().getProperty(property);
        if (property1 == null)
            throw new IllegalArgumentException(property);

        // Property exists in state container, we're good
        Collection<?> allowedValues = property1.getPossibleValues();
        boolean anyMatch = allowedValues.stream().anyMatch(v -> val.test((Comparable<?>) v));
        if(!anyMatch) {
            CompactCrafting.LOGGER.warn("Failed to allow filter: No values would be valid for property [{}]", property);
            return;
        }

        this.allowAll = false;
        filters.put(property, val);
    }

    public boolean filterMatches(BlockState state) {
        if(allowAll) return true;
        for(Property<?> prop : state.getProperties()) {
            String name = prop.getName();

            // If it's not in the whitelist, we don't care about what the value is
            if (!filters.containsKey(name))
                continue;

            Comparable<?> val = state.getValue(prop);
            boolean matches = filters.get(name).test(val);
            if(!matches) return false;
        }

        return true;
    }
}
