package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeBlocksTests {

    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateBlocksInstance(final GameTestHelper test) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(test, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test));

        final int compCount = blocks.getNumberKnownComponents();

        if (0 == compCount)
            test.fail("No components registered.");

        test.succeed();
    }


    @GameTest(template = "recipes/ender_crystal")
    public static void CanRebuildTotals(final GameTestHelper test) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(test, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test));

        try {
            blocks.rebuildComponentTotals();
            test.succeed();
        } catch (Exception e) {
            test.fail("Rebuilding component totals failed.");
        }
    }


    @GameTest(template = "recipes/ender_crystal")
    public static void CanSlice(final GameTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
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


    @GameTest(template = "recipes/ender_crystal")
    public static void CanSliceAndOffset(final GameTestHelper test) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(test, "ender_crystal").orElseThrow();

        final var fieldBounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test);
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


    @GameTest(template = "recipes/ender_crystal")
    public static void CanCreateWithUnknownComponents(final GameTestHelper test) {
        // defines G and O as components - "-" should be an unknown position in this recipe
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(test, "ender_crystal").orElseThrow();

        final Set<String> keys = components.getBlockComponents().keySet();
        if (2 != keys.size())
            test.fail("Expected exactly 2 registered block components.");

        final var bounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test);
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
