package dev.compactmods.crafting.tests.recipes.layers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.tests.testers.MultiLayerRecipeTestHelper;
import dev.compactmods.crafting.tests.testers.TestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeBlocksTests {

    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateBlocksInstance(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("ender_crystal")
                .forFieldOfSize(MiniaturizationFieldSize.MEDIUM);

        final var blocks = testHelper.blocks();
        final int compCount = blocks.getNumberKnownComponents();

        if (0 == compCount)
            test.fail("No components registered.");

        test.succeed();
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void CanRebuildTotals(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("ender_crystal")
                .forFieldOfSize(MiniaturizationFieldSize.MEDIUM);

        final var blocks = testHelper.blocks();

        try {
            blocks.rebuildComponentTotals();
            test.succeed();
        } catch (Exception e) {
            test.fail("Rebuilding component totals failed.");
        }
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void CanSlice(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("ender_crystal")
                .forFieldOfSize(MiniaturizationFieldSize.MEDIUM);

        final var blocks = testHelper.blocks();

        final IRecipeBlocks slice = blocks.slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0))
                .normalize();

        final Optional<String> c0 = slice.getComponentAtPosition(BlockPos.ZERO);
        if (c0.isEmpty()) {
            test.fail("Expected glass component to transfer to new blocks instance.");
            return;
        }

        if (!"G".equals(c0.get()))
            test.fail("Expected glass component.");

        try {
            final Map<String, Integer> totals = slice.getKnownComponentTotals();
            if (1 != totals.size())
                test.fail("Expected exactly one component in totals list");

            if (!totals.containsKey("G"))
                test.fail("Totals did not contain glass component.");

            if(25 != totals.get("G"))
                test.fail("Expected 25 glass blocks. Got " + totals.get("G"));
        } catch (Exception e) {
            test.fail("Caught exception: " + e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void CanSliceAndOffset(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("ender_crystal")
                .forFieldOfSize(MiniaturizationFieldSize.MEDIUM);

        final var blocks = testHelper.blocks();

        final var newBounds = testHelper.getLayerBounds(2);
        final IRecipeBlocks slice = blocks.slice(newBounds).normalize();

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

    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateWithUnknownComponents(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("ender_crystal")
                .forFieldOfSize(MiniaturizationFieldSize.MEDIUM);

        // defines G and O as components - "-" should be an unknown component in this recipe
        IRecipeComponents components = testHelper.components();

        final Set<String> keys = components.getBlockComponents().keySet();
        if (2 != keys.size())
            test.fail("Expected exactly 2 registered block components.");

        final var blocks1 = testHelper.blocks();

        final var blocks = blocks1
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
