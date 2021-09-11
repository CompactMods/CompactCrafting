package dev.compactmods.crafting.tests.recipes.layers;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.world.TestBlockReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class RecipeLayerBlocksTests {

    @Test
    @Tag("minecraft")
    void CanCreate() {
        TestBlockReader reader = RecipeTestUtil.getBlockReader("recipes/ender_crystal.json");
        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, MiniaturizationFieldSize.MEDIUM.getBoundsAtOrigin());

        Assertions.assertNotNull(blocks);

        final int compCount = Assertions.assertDoesNotThrow(blocks::getNumberKnownComponents);

        Assertions.assertNotEquals(0, compCount);

    }
}
