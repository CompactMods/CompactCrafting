package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RecipeLayerUtilTests {

    @Test
    @Tag("minecraft")
    void CanRotate() {
        TestRecipeLayerBlocks harness = RecipeTestUtil.getLayerHarness("worlds/basic_harness_5x.json");
        Assertions.assertNotNull(harness);

        final IRecipeLayerBlocks rotatedClockwise = RecipeLayerUtil.rotate(harness, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        final Set<BlockPos> originalPositions = harness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertNotEquals(originalPositions, rotatedPositions);
    }

    @Test
    @Tag("minecraft")
    void CanRotateWithUnknownComponent() {
        TestRecipeLayerBlocks harness = RecipeTestUtil.getLayerHarness("worlds/unknown_component.json");
        Assertions.assertNotNull(harness);

        boolean identified = harness.allIdentified();
        Assertions.assertFalse(identified, "Missing components were not identified.");

        final IRecipeLayerBlocks rotatedClockwise = RecipeLayerUtil.rotate(harness, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        Assertions.assertFalse(rotatedClockwise.allIdentified(), "Missing components were not identified post-rotate.");
        final Set<BlockPos> iron = rotatedClockwise.getPositionsForComponent("Ir")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        // Should have one mapping
        Assertions.assertFalse(iron.isEmpty());
        Assertions.assertEquals(1, iron.size());
    }

    @Test
    @Tag("minecraft")
    void NonRotationCreatesCopiedInstance() {
        TestRecipeLayerBlocks harness = RecipeTestUtil.getLayerHarness("worlds/basic_harness_5x.json");
        Assertions.assertNotNull(harness);

        final IRecipeLayerBlocks rotatedHarness = RecipeLayerUtil.rotate(harness, Rotation.NONE);
        Assertions.assertNotNull(rotatedHarness);

        final Set<BlockPos> originalPositions = harness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedHarness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertEquals(originalPositions, rotatedPositions);

        Assertions.assertNotSame(harness, rotatedHarness);
    }


}
