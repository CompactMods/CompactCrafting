package dev.compactmods.crafting.tests.recipes.components;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComponentPositionLookupTests {

    @Test
    void CanCreate() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        Assertions.assertNotNull(lookup);
    }

    @Test
    void CanAddSingleComponent() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "G");

        // Full component list should contain the component key (G) and the position, at least
        Assertions.assertTrue(lookup.getComponents().contains("G"));
        Assertions.assertTrue(lookup.containsLocation(BlockPos.ZERO));

        // Reverse lookup by position should return "G" inside an optional
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);
        Assertions.assertTrue(key.isPresent());
        Assertions.assertEquals("G", key.get());

        // We only added one position referencing G, so make sure it's in the list and there's only one
        final Set<BlockPos> positions = lookup.getPositionsForComponent("G")
                .map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertEquals(1, positions.size());
        Assertions.assertTrue(positions.contains(BlockPos.ZERO));

        // All positions - Only one should be registered
        final Set<BlockPos> allPositions = lookup.getAllPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertEquals(1, allPositions.size());
        Assertions.assertTrue(allPositions.contains(BlockPos.ZERO));
    }

    @Test
    void UnknownPositionConsideredEmpty() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);

        Assertions.assertFalse(key.isPresent());
    }

    @Test
    void NullStringLookupReturnsEmpty() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Stream<BlockPos> positionsForComponent = lookup.getPositionsForComponent(null);

        Assertions.assertEquals(0, positionsForComponent.count());
    }

    @Test
    void CanCreateAndCacheTotals() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "C");

        // First pass - should calculate successfully
        final Map<String, Integer> totals = Assertions.assertDoesNotThrow(lookup::getComponentTotals);
        Assertions.assertTrue(totals.containsKey("C"));
        Assertions.assertEquals(1, totals.get("C"));

        // Second pass - should return the already built totals object
        final Map<String, Integer> secondPass = Assertions.assertDoesNotThrow(lookup::getComponentTotals);
        Assertions.assertSame(totals, secondPass);
        Assertions.assertTrue(secondPass.containsKey("C"));
        Assertions.assertEquals(1, secondPass.get("C"));
    }

}
