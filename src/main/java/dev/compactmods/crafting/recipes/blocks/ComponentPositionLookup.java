package dev.compactmods.crafting.recipes.blocks;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;

public class ComponentPositionLookup {

    protected final Map<BlockPos, String> components;
    protected final Map<String, Integer> componentTotals;

    public static final Codec<ComponentPositionLookup> CODEC = new ComponentPositionLookupCodec();

    public ComponentPositionLookup() {
        this.components = new HashMap<>();
        this.componentTotals = new HashMap<>();
    }

    public void add(BlockPos location, String component) {
        components.putIfAbsent(location, component);
        componentTotals.putIfAbsent(component, 0);

        // Increment totals to keep in sync
        componentTotals.put(component, componentTotals.get(component) + 1);
    }

    public Collection<String> getComponents() {
        return componentTotals.keySet();
    }

    public Stream<BlockPos> getAllPositions() {
        return components.keySet().stream();
    }

    public boolean containsLocation(BlockPos location) {
        return components.containsKey(location);
    }

    public Map<String, Integer> getComponentTotals() {
        return this.componentTotals;
    }

    void rebuildComponentTotals() {
        componentTotals.clear();
        final Map<String, Integer> totals = components.entrySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                // Group by map value (aka component key)
                                Map.Entry::getValue,

                                // Map keys (the blockpos entries) are summed up (like list::size)
                                Collectors.mapping(
                                        Map.Entry::getKey,
                                        Collectors.reducing(0, e -> 1, Integer::sum)
                                )
                        )
                );

        componentTotals.putAll(totals);
    }

    public Optional<String> getRequiredComponentKeyForPosition(BlockPos pos) {
        return Optional.ofNullable(components.get(pos));
    }

    /**
     * Get a collection of positions that are filled by a given component.
     *
     * @return
     */
    public Stream<BlockPos> getPositionsForComponent(String component) {
        if (component == null)
            return Stream.empty();

        return components.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(component))
                .map(Map.Entry::getKey);
    }

    public void remove(String component) {
        final Set<BlockPos> positions = getPositionsForComponent(component).collect(Collectors.toSet());
        positions.forEach(components::remove);
        componentTotals.remove(component);
    }
}
