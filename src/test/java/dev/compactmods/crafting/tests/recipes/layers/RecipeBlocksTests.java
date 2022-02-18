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
import org.junit.jupiter.api.Assertions;

public class RecipeBlocksTests {

    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanCreate(final GameTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper));

        Assertions.assertNotNull(blocks);

        final int compCount = Assertions.assertDoesNotThrow(blocks::getNumberKnownComponents);

        Assertions.assertNotEquals(0, compCount);
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanRebuildTotals(final GameTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper));

        Assertions.assertNotNull(blocks);

        Assertions.assertDoesNotThrow(blocks::rebuildComponentTotals);
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanSlice(final GameTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        Assertions.assertNotNull(blocks);

        final IRecipeBlocks slice = blocks.slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0))
                .normalize();

        Assertions.assertNotNull(slice);

        final Optional<String> c0 = slice.getComponentAtPosition(BlockPos.ZERO);
        Assertions.assertTrue(c0.isPresent(), "Expected glass component to transfer to new blocks instance.");
        Assertions.assertEquals("G", c0.get());

        final Map<String, Integer> totals = Assertions.assertDoesNotThrow(slice::getKnownComponentTotals);
        Assertions.assertEquals(1, totals.size());
        Assertions.assertTrue(totals.containsKey("G"));
        Assertions.assertEquals(25, totals.get("G"));
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanSliceAndOffset(final GameTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final var fieldBounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, fieldBounds);

        Assertions.assertNotNull(blocks);

        final IRecipeBlocks slice = blocks.slice(BlockSpaceUtil.getLayerBounds(fieldBounds, 2)).normalize();

        Assertions.assertNotNull(slice);

        final Optional<String> c0 = slice.getComponentAtPosition(BlockPos.ZERO);
        Assertions.assertTrue(c0.isPresent(), "Expected glass component to transfer to new blocks instance.");
        Assertions.assertEquals("G", c0.get());

        final Map<String, Integer> totals = Assertions.assertDoesNotThrow(slice::getKnownComponentTotals);
        Assertions.assertEquals(2, totals.size());
        Assertions.assertTrue(totals.containsKey("G"));
        Assertions.assertEquals(16, totals.get("G"));
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanCreateWithUnknownComponents(final GameTestHelper helper) {
        // defines G and O as components - "-" should be an unknown position in this recipe
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final Set<String> keys = components.getBlockComponents().keySet();
        Assertions.assertEquals(2, keys.size());

        final var bounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final var blocks1 = RecipeBlocks.create(helper.getLevel(), components, bounds);

        final IRecipeBlocks blocks = blocks1.normalize()
                .slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 2))
                .normalize();

        final Set<BlockPos> unknownSet = blocks.getUnmappedPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if (!unknownSet.isEmpty())
            helper.fail("Expected no unmapped positions - undefined positions are air.");
    }
}
