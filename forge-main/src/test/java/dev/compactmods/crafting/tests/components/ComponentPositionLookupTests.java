package dev.compactmods.crafting.tests.components;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.tests.GameTestTemplates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ComponentPositionLookupTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanSerializeWithCodec(final GameTestHelper test) {
        ComponentPositionLookup lookup = new ComponentPositionLookup();

        lookup.add(BlockPos.ZERO, "A");
        lookup.add(new BlockPos(2, 0, 2), "B");

        final JsonElement serialized = ComponentPositionLookup.CODEC.encodeStart(JsonOps.INSTANCE, lookup)
                .getOrThrow(false, test::fail);

        if (null == serialized)
            test.fail("Serialized value was null");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanAddSingleComponent(final GameTestHelper test) {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "G");

        // Full component list should contain the component key (G) and the position, at least
        final Collection<String> componentList = GameTestAssertions.assertDoesNotThrow(lookup::getComponents);
        if (null == componentList) ;
        GameTestAssertions.assertTrue(componentList.contains("G"), "Expected 'G' to be in component list.");
        GameTestAssertions.assertTrue(lookup.containsLocation(BlockPos.ZERO), "Expected BP.ZERO to be in component lookup.");

        // Reverse lookup by position should return "G" inside an optional
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);
        GameTestAssertions.assertTrue(key.isPresent(), "Expected to find a component 'G', did not find one.");
        GameTestAssertions.assertEquals("G", key.get());

        // We only added one position referencing G, so make sure it's in the list and there's only one
        final Set<BlockPos> positions = lookup.getPositionsForComponent("G")
                .map(BlockPos::immutable).collect(Collectors.toSet());
        GameTestAssertions.assertEquals(1, positions.size());
        GameTestAssertions.assertTrue(positions.contains(BlockPos.ZERO), "Expected BP.ZERO to be in component position list.");

        // All positions - Only one should be registered
        final Set<BlockPos> allPositions = lookup.getAllPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        GameTestAssertions.assertEquals(1, allPositions.size());
        GameTestAssertions.assertTrue(allPositions.contains(BlockPos.ZERO), "Expected lookup to have BP.ZERO in its position map.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void UnknownPositionConsideredEmpty(final GameTestHelper test) {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Optional<String> key = lookup.getRequiredComponentKeyForPosition(BlockPos.ZERO);

        GameTestAssertions.assertFalse(key.isPresent());
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void null_position_lookup_finds_nothing(final GameTestHelper test) {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        final Stream<BlockPos> positionsForComponent = lookup.getPositionsForComponent(null);

        if(positionsForComponent.findAny().isPresent())
            test.fail("Expected null component to return no positions found.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreateAndCacheTotals(final GameTestHelper test) {
        ComponentPositionLookup lookup = new ComponentPositionLookup();
        lookup.add(BlockPos.ZERO, "C");

        // First pass - should calculate successfully
        final Map<String, Integer> totals = GameTestAssertions.assertDoesNotThrow(lookup::getComponentTotals);
        GameTestAssertions.assertTrue(totals.containsKey("C"));
        GameTestAssertions.assertEquals(1, totals.get("C"));

        // Second pass - should return the already built totals object
        final Map<String, Integer> secondPass = GameTestAssertions.assertDoesNotThrow(lookup::getComponentTotals);
        if(totals != secondPass)
            test.fail("Instances did not match.");

        GameTestAssertions.assertTrue(secondPass.containsKey("C"));
        GameTestAssertions.assertEquals(1, secondPass.get("C"));

        test.succeed();
    }
}
