package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayerBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.world.TestBlockReader;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RecipeLayerUtilTests {

    @Test
    @Tag("minecraft")
    void CanRotate() {
        TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_harness_5x.json");
        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final IRecipeLayerBlocks rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertNotEquals(originalPositions, rotatedPositions);
    }

    @Test
    @Tag("minecraft")
    void CanRotateWithUnknownComponent() {
        TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/unknown_component.json");
        Assertions.assertNotNull(reader);

        final IRecipeComponents recipeComponents = reader.source.getComponents();
        recipeComponents.unregisterBlock("Ir");

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.SMALL, 0));

        boolean identified = blocks.allIdentified();
        Assertions.assertFalse(identified, "Missing components were not identified.");

        final IRecipeLayerBlocks rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        Assertions.assertFalse(rotatedClockwise.allIdentified(), "Missing components were identified post-rotate.");
        final Set<BlockPos> iron = rotatedClockwise.getPositionsForComponent("Ir")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        // Should not have a mapping - the position does not have a recipe mapping
        Assertions.assertTrue(iron.isEmpty());
        final Set<BlockPos> unmapped = rotatedClockwise.getUnmappedPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertFalse(unmapped.isEmpty());
        Assertions.assertEquals(1, unmapped.size());
        Assertions.assertTrue(unmapped.contains(new BlockPos(2, 0, 0)));
    }

    @Test
    @Tag("minecraft")
    void NonRotationCreatesCopiedInstance() {
        TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_harness_5x.json");
        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final IRecipeLayerBlocks rotatedHarness = RecipeLayerUtil.rotate(blocks, Rotation.NONE);
        Assertions.assertNotNull(rotatedHarness);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedHarness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertEquals(originalPositions, rotatedPositions);

        Assertions.assertNotSame(blocks, rotatedHarness);
    }


}
