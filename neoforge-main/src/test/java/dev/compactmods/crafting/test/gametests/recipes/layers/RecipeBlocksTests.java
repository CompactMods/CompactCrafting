package dev.compactmods.crafting.test.gametests.recipes.layers;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.test.gametests.util.CompactGameTestHelper;
import dev.compactmods.crafting.test.junit.recipes.util.JUnitTestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ForEachTest(groups = "recipe_blocks")
public class RecipeBlocksTests {

    @TestHolder

    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateBlocksInstance(final CompactGameTestHelper test) {
        IRecipeComponents components = JUnitTestHelper.getRecipeFromFile("ender_crystal")
                .map(MiniaturizationRecipe::getComponents)
                .orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM));

        final int compCount = blocks.getNumberKnownComponents();

        if (0 == compCount)
            test.fail("No components registered.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = "recipes/ender_crystal")
    public static void CanRebuildTotals(final CompactGameTestHelper test) {
        IRecipeComponents components = JUnitTestHelper.getRecipeFromFile("ender_crystal")
                .map(MiniaturizationRecipe::getComponents)
                .orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFieldBounds(MiniaturizationFieldSize.MEDIUM));

        try {
            blocks.rebuildComponentTotals();
            test.succeed();
        } catch (Exception e) {
            test.fail("Rebuilding component totals failed.");
        }
    }

    @TestHolder
    @GameTest(template = "recipes/ender_crystal")
    public static void CanSlice(final CompactGameTestHelper helper) {
        IRecipeComponents components = JUnitTestHelper.getRecipeFromFile("ender_crystal")
                .map(MiniaturizationRecipe::getComponents)
                .orElse(null);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, helper.getFieldBounds(MiniaturizationFieldSize.MEDIUM))
                .normalize();

        final IRecipeBlocks slice = blocks.slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0))
                .normalize();

        final Optional<String> c0 = slice.getComponentAtPosition(BlockPos.ZERO);
        if (c0.isEmpty())
            helper.fail("Expected glass component to transfer to new blocks instance.");

        if (!"G".equals(c0.get()))
            helper.fail("Expected glass component.");

        try {
            final Map<String, Integer> totals = slice.getKnownComponentTotals();
            if (1 != totals.size())
                helper.fail("Expected exactly one component in totals list");

            if (!totals.containsKey("G"))
                helper.fail("Totals did not contain glass component.");

            if(25 != totals.get("G"))
                helper.fail("Expected 25 glass blocks. Got " + totals.get("G"));
        } catch (Exception e) {
            helper.fail("Caught exception: " + e.getMessage());
        }

        helper.succeed();
    }

    @TestHolder
    @GameTest(template = "recipes/ender_crystal")
    public static void CanSliceAndOffset(final CompactGameTestHelper test) {
        IRecipeComponents components = JUnitTestHelper.getRecipeFromFile("ender_crystal")
                .map(MiniaturizationRecipe::getComponents)
                .orElseThrow();

        final var fieldBounds = test.getFieldBounds(MiniaturizationFieldSize.MEDIUM);
        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, fieldBounds);

        final IRecipeBlocks slice = blocks.slice(BlockSpaceUtil.getLayerBounds(fieldBounds, 2)).normalize();

        final Optional<String> c0 = slice.getComponentAtPosition(BlockPos.ZERO);
        if (c0.isEmpty())
            test.fail("Expected glass component to transfer to new blocks instance.");

        if (!c0.get().equals("G"))
            test.fail("Expected glass component key to be 'G'");

        final Map<String, Integer> totals = slice.getKnownComponentTotals();
        if (2 != totals.size())
            test.fail("Expected 2 known components to be found.");

        if (!totals.containsKey("G"))
            test.fail("Expected glass block (G) to be in found components.");

        if (16 != totals.get("G"))
            test.fail("Expected glass blocks (G) to have 16 found positions");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateWithUnknownComponents(final CompactGameTestHelper test) {
        // defines G and O as components - "-" should be an unknown position in this recipe
        IRecipeComponents components = JUnitTestHelper.getRecipeFromFile("ender_crystal")
                .map(MiniaturizationRecipe::getComponents)
                .orElseThrow();

        final Set<String> keys = components.getBlockComponents().keySet();
        if (2 != keys.size())
            test.fail("Expected exactly 2 registered block components.");

        final var bounds = test.getFieldBounds(MiniaturizationFieldSize.MEDIUM);
        final var blocks1 = RecipeBlocks.create(test.getLevel(), components, bounds);

        final IRecipeBlocks blocks = blocks1.normalize()
                .slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 2))
                .normalize();

        final Set<BlockPos> unknownSet = blocks.getUnmappedPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if (!unknownSet.isEmpty())
            test.fail("Expected no unmapped positions - undefined positions are air.");

        test.succeed();
    }
}
