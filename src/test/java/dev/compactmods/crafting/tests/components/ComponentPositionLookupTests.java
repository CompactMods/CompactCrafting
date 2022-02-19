package dev.compactmods.crafting.tests.components;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComponentPositionLookupTests {

    @Test
    void CanCreatePosLookup() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        Assertions.assertNotNull(lookup);
    }

    @Test
    void CanSerializeWithCodec() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        Assertions.assertNotNull(lookup);
        lookup.add(BlockPos.ZERO, "A");
        lookup.add(new BlockPos(2, 0, 2), "B");

        final JsonElement serialized = ComponentPositionLookup.CODEC.encodeStart(JsonOps.INSTANCE, lookup)
                .getOrThrow(false, Assertions::fail);

        Assertions.assertNotNull(serialized);
    }

    @Test
    void CanAddSingleComponent() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "G");

        // Full component list should contain the component key (G) and the position, at least
        final Collection<String> componentList = Assertions.assertDoesNotThrow(lookup::getComponents);
        Assertions.assertNotNull(componentList);
        Assertions.assertTrue(componentList.contains("G"), "Expected 'G' to be in component list.");
        Assertions.assertTrue(lookup.containsLocation(BlockPos.ZERO), "Expected BP.ZERO to be in component lookup.");

        // Reverse lookup by position should return "G" inside an optional
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);
        Assertions.assertTrue(key.isPresent(), "Expected to find a component 'G', did not find one.");
        Assertions.assertEquals("G", key.get());

        // We only added one position referencing G, so make sure it's in the list and there's only one
        final Set<BlockPos> positions = lookup.getPositionsForComponent("G")
                .map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertEquals(1, positions.size());
        Assertions.assertTrue(positions.contains(BlockPos.ZERO), "Expected BP.ZERO to be in component position list.");

        // All positions - Only one should be registered
        final Set<BlockPos> allPositions = lookup.getAllPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertEquals(1, allPositions.size());
        Assertions.assertTrue(allPositions.contains(BlockPos.ZERO), "Expected lookup to have BP.ZERO in its position map.");
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
