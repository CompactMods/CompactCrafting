package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("recipes")
public class RecipeBlocksTests {



    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanCreate(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper));

        Assertions.assertNotNull(blocks);

        final int compCount = Assertions.assertDoesNotThrow(blocks::getNumberKnownComponents);

        Assertions.assertNotEquals(0, compCount);
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanRebuildTotals(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper));

        Assertions.assertNotNull(blocks);

        Assertions.assertDoesNotThrow(blocks::rebuildComponentTotals);
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanSlice(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
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

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanSliceAndOffset(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final AxisAlignedBB fieldBounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, fieldBounds);

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

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanCreateWithUnknownComponents(IntegrationTestHelper helper) {
        // defines G and O as components - "-" should be an unknown position in this recipe
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipe(helper, "ender_crystal").orElse(null);

        final Set<String> keys = components.getBlockComponents().keySet();
        Assertions.assertEquals(2, keys.size());

        final AxisAlignedBB bounds = RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final IRecipeBlocks blocks1 = RecipeBlocks.create(helper.getWorld(), components, bounds);

        final IRecipeBlocks blocks = blocks1.normalize()
                .slice(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 2))
                .normalize();

        final Set<BlockPos> unknownSet = blocks.getUnmappedPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        helper.assertTrue(unknownSet::isEmpty, "Expected no unmapped positions - undefined positions are air.");
    }
}
