package dev.compactmods.crafting.test.junit.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentPositionLookupTests {

    @Test
    public void CanSerializeWithCodec() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();

        lookup.add(BlockPos.ZERO, "A");
        lookup.add(new BlockPos(2, 0, 2), "B");

        final JsonElement serialized = ComponentPositionLookup.CODEC.encodeStart(JsonOps.INSTANCE, lookup)
                .getOrThrow();

        if (null == serialized)
            Assertions.fail("Serialized value was null");
    }

    @Test
    public void CanAddSingleComponent() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "G");

        // Full component list should contain the component key (G) and the position, at least
        final Collection<String> componentList = Assertions.assertDoesNotThrow(lookup::getComponents);
        if (null == componentList) ;
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
    public void UnknownPositionConsideredEmpty() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);

        Assertions.assertFalse(key.isPresent());
    }

    @Test
    public void null_position_lookup_finds_nothing() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Stream<BlockPos> positionsForComponent = lookup.getPositionsForComponent(null);

        if (positionsForComponent.findAny().isPresent())
            Assertions.fail("Expected null component to return no positions found.");

    }

    @Test
    public void CanCreateAndCacheTotals() {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "C");

        // First pass - should calculate successfully
        final var totals = Assertions.assertDoesNotThrow(lookup::getComponentTotals);
        Assertions.assertTrue(totals.containsKey("C"));
        Assertions.assertEquals(1, totals.get("C"));

        // Second pass - should return the already built totals object
        final var secondPass = Assertions.assertDoesNotThrow(lookup::getComponentTotals);
        if (totals != secondPass)
            Assertions.fail("Instances did not match.");

        Assertions.assertTrue(secondPass.containsKey("C"));
        Assertions.assertEquals(1, secondPass.get("C"));

    }
}
