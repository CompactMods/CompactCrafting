package dev.compactmods.crafting.tests.recipes.layers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@IntegrationTestClass("layers")
public class HollowLayerTests {

    private HollowComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile(filename);

        return HollowComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow(false, Assertions::fail);
    }

    @Test
    @Tag("minecraft")
    void CanCreateHollowLayerWithConstructor() {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        Assertions.assertNotNull(layer);
    }

    @Test
    @Tag("minecraft")
    void HollowComponentCountsAreCorrectForFieldSize() {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        Assertions.assertNotNull(layer);

        HashMap<MiniaturizationFieldSize, Integer> counts = new HashMap<>();
        for (MiniaturizationFieldSize size : MiniaturizationFieldSize.VALID_SIZES) {
            int all = (int) Math.pow(size.getDimensions(), 2);
            int inner = (int) Math.pow(size.getDimensions() - 2, 2);

            int expected = all - inner;

            // Make sure we can set a layer size for the initialization check below
            layer.setRecipeDimensions(size);
            Assertions.assertEquals(expected, layer.getNumberFilledPositions(), "Filled position count did not match for size: " + size);

            final Map<String, Integer> totals = Assertions.assertDoesNotThrow(layer::getComponentTotals);
            Assertions.assertTrue(totals.containsKey("A"), "Component list did not contain wall.");
            Assertions.assertEquals(expected, totals.get("A"), "Outer totals did not match for size: " + size);
        }
    }

    @Test
    @Tag("minecraft")
    void HollowPositionalInquiries() {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        Assertions.assertNotNull(layer);

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        // A wall position should match the layer's component key
        final Optional<String> comp = layer.getComponentForPosition(BlockPos.ZERO);
        Assertions.assertTrue(comp.isPresent());
        comp.ifPresent(c -> {
            Assertions.assertEquals("A", c);
        });

        // Center position should be considered empty
        final Optional<String> center = layer.getComponentForPosition(new BlockPos(1, 0, 1));
        Assertions.assertFalse(center.isPresent());

        // Bad component keys just return empty streams
        final Stream<BlockPos> xPositions = Assertions.assertDoesNotThrow(() -> layer.getPositionsForComponent("X"));
        Assertions.assertNotNull(xPositions);
        Assertions.assertEquals(0, xPositions.count());
    }

    @Test
    @Tag("minecraft")
    void HollowCanReturnComponentPositions() {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        Assertions.assertNotNull(layer);

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        final Stream<BlockPos> list = layer.getPositionsForComponent("A");
        Assertions.assertNotNull(list);

        final Set<BlockPos> positionSet = list.map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertEquals(8, positionSet.size());

        final Set<BlockPos> wallPositions = BlockSpaceUtil.getWallPositions(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.SMALL, 0))
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertEquals(wallPositions, positionSet);
    }

    @Tag("minecraft")
    @IntegrationTest("empty_medium")
    void HollowFailsIfPrimaryComponentMissing(IntegrationTestHelper helper) {
        final BlockPos zeroPoint = helper.relativePos(BlockPos.ZERO).orElse(BlockPos.ZERO);
        helper.setBlockState(BlockPos.ZERO, Blocks.AIR.defaultBlockState());

        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("-", new EmptyBlockComponent());

        final AxisAlignedBB bounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0).move(zeroPoint);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final Boolean result = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));

        Assertions.assertFalse(result, "Layer matched despite not having any matchable components.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls")
    void HollowMatchesWorldDefinitionExactly(IntegrationTestHelper helper) {
        final BlockPos zeroPoint = helper.relativePos(BlockPos.ZERO).orElse(BlockPos.ZERO);

        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        final AxisAlignedBB bounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0).move(zeroPoint);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (!matched) {
            helper.fail("Hollow did not pass perfect match.");
        }
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls")
    void HollowFailsIfAnyComponentsUnidentified(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        final AxisAlignedBB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        Assertions.assertFalse(matched, "Hollow did not pass perfect match.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls")
    @DisplayName("Hollow - Bad Wall Block")
    void HollowFailsIfWorldHasBadWallBlock(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        // register gold block to get past the unknown component early fail
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));

        helper.setBlockState(BlockPos.ZERO, Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        Assertions.assertFalse(matches, "Hollow matched when BP.ZERO was a different block.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void HollowFailsIfMoreThanOneComponentAndCenterNotEmpty(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("W", new BlockComponent(Blocks.GLASS));

        // we need to register the obsidian block here; the layer will fail early otherwise
        // since otherwise, the center block will be unmatched
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("W");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        Assertions.assertFalse(matches, "Hollow matched when center block was a different block.");
    }
}
