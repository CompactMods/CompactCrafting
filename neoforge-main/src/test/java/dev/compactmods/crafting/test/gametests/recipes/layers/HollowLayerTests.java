package dev.compactmods.crafting.test.gametests.recipes.layers;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.test.gametests.GameTestAssertions;
import dev.compactmods.crafting.test.gametests.CMTestStructures;
import dev.compactmods.crafting.test.gametests.util.CompactGameTestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ForEachTest(groups = "layers")
public class HollowLayerTests {

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void CanCreateHollowLayerWithConstructor(final GameTestHelper test) {
        new HollowComponentRecipeLayer("A");
        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void HollowComponentCountsAreCorrectForFieldSize(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");

        HashMap<MiniaturizationFieldSize, Integer> counts = new HashMap<>();
        for (MiniaturizationFieldSize size : MiniaturizationFieldSize.VALID_SIZES) {
            int all = (int) Math.pow(size.getDimensions(), 2);
            int inner = (int) Math.pow(size.getDimensions() - 2, 2);

            int expected = all - inner;

            // Make sure we can set a layer size for the initialization check below
            layer.setRecipeDimensions(size);
            if (!Objects.equals(expected, layer.getNumberFilledPositions()))
                test.fail("Filled position count did not match for size: " + size);

            final Map<String, Integer> totals = layer.getComponentTotals();
            test.assertTrue(totals.containsKey("A"), "Component list did not contain wall.");
            if (!Objects.equals(expected, totals.get("A")))
                test.fail("Outer totals did not match for size: " + size);

            test.succeed();
        }
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void HollowPositionalInquiries(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        // A wall position should match the layer's component key
        final Optional<String> comp = layer.getComponentForPosition(BlockPos.ZERO);
        test.assertTrue(comp.isPresent(), "component exists");
        comp.ifPresent(c -> {
            test.assertValueEqual(c, "A", "Component did not match.");
        });

        // Center position should be considered empty
        final Optional<String> center = layer.getComponentForPosition(new BlockPos(1, 0, 1));
        test.assertFalse(center.isPresent(), "center is present");

        // Bad component keys just return empty streams
        final Stream<BlockPos> xPositions = layer.getPositionsForComponent("X");
        if (xPositions.findAny().isPresent())
            test.fail("Expected no positions to be found for component.");

        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void returns_component_positions(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        final Set<BlockPos> positionSet = layer.getPositionsForComponent("A")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        test.assertValueEqual(positionSet.size(), 8, "Position count should be equal to 8");

        final Set<BlockPos> wallPositions = BlockSpaceUtil.getWallPositions(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.SMALL, 0))
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        test.assertValueEqual(positionSet, wallPositions, "Matched wall positions did not match expected value.");

        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.FIVE_CUBED)
    public static void HollowFailsIfPrimaryComponentMissing(final GameTestHelper test) {
        final BlockPos zeroPoint = test.relativePos(BlockPos.ZERO);
        test.setBlock(BlockPos.ZERO, Blocks.AIR.defaultBlockState());

        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("-", new EmptyBlockComponent());

        final AABB bounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0).move(zeroPoint);

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final var result = layer.matches(components, blocks);

        if (result)
            test.fail("Layer matched despite not having any matchable components.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS)
    public static void HollowMatchesWorldDefinitionExactly(final CompactGameTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        final AABB bounds = helper.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (!matched) {
            helper.fail("Hollow did not pass perfect match.");
        }

        helper.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS)
    public static void HollowFailsIfAnyComponentsUnidentified(final CompactGameTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        final AABB bounds = helper.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (matched)
            helper.fail("Hollow matched, despite having unidentified, non-air components.");

        helper.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS)
    public static void HollowFailsIfWorldHasBadWallBlock(final CompactGameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        // register gold block to get past the unknown component early fail
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));

        test.setBlock(BlockPos.ZERO.above(), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when BP.ZERO was a different block.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public static void HollowFailsIfMoreThanOneComponentAndCenterNotEmpty(final CompactGameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("W", new BlockComponent(Blocks.GLASS));

        // we need to register the obsidian block here; the layer will fail early otherwise
        // since otherwise, the center block will be unmatched
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("W");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when center block was a different block.");

        test.succeed();
    }
}
