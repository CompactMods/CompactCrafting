package dev.compactmods.crafting.tests.recipes.layers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.HollowComponentRecipeLayer;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.tests.testers.MultiLayerRecipeTestHelper;
import dev.compactmods.crafting.tests.testers.TestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class HollowLayerTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreateHollowLayerWithConstructor(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        GameTestAssertions.assertNotNull(layer);
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void HollowComponentCountsAreCorrectForFieldSize(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        GameTestAssertions.assertNotNull(layer);

        HashMap<MiniaturizationFieldSize, Integer> counts = new HashMap<>();
        for (MiniaturizationFieldSize size : MiniaturizationFieldSize.VALID_SIZES) {
            int all = (int) Math.pow(size.getDimensions(), 2);
            int inner = (int) Math.pow(size.getDimensions() - 2, 2);

            int expected = all - inner;

            // Make sure we can set a layer size for the initialization check below
            layer.setRecipeDimensions(size);
            if (!Objects.equals(expected, layer.getNumberFilledPositions()))
                test.fail("Filled position count did not match for size: " + size);

            final Map<String, Integer> totals = GameTestAssertions.assertDoesNotThrow(layer::getComponentTotals);
            GameTestAssertions.assertTrue(totals.containsKey("A"), "Component list did not contain wall.");
            if (!Objects.equals(expected, totals.get("A")))
                test.fail("Outer totals did not match for size: " + size);

            test.succeed();
        }
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void HollowPositionalInquiries(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        GameTestAssertions.assertNotNull(layer);

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        // A wall position should match the layer's component key
        final Optional<String> comp = layer.getComponentForPosition(BlockPos.ZERO);
        GameTestAssertions.assertTrue(comp.isPresent());
        comp.ifPresent(c -> {
            GameTestAssertions.assertEquals("A", c);
        });

        // Center position should be considered empty
        final Optional<String> center = layer.getComponentForPosition(new BlockPos(1, 0, 1));
        GameTestAssertions.assertFalse(center.isPresent());

        // Bad component keys just return empty streams
        final Stream<BlockPos> xPositions = GameTestAssertions.assertDoesNotThrow(() -> layer.getPositionsForComponent("X"));
        GameTestAssertions.assertNotNull(xPositions);
        if (xPositions.findAny().isPresent())
            test.fail("Expected no positions to be found for component.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void returns_component_positions(final GameTestHelper test) {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        GameTestAssertions.assertNotNull(layer);

        // Make sure we can set a layer size for the initialization check below
        layer.setRecipeDimensions(MiniaturizationFieldSize.SMALL);

        final Stream<BlockPos> list = layer.getPositionsForComponent("A");
        GameTestAssertions.assertNotNull(list);

        final Set<BlockPos> positionSet = list.map(BlockPos::immutable).collect(Collectors.toSet());

        GameTestAssertions.assertEquals(8, positionSet.size());

        final Set<BlockPos> wallPositions = BlockSpaceUtil.getWallPositions(BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.SMALL, 0))
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        GameTestAssertions.assertEquals(wallPositions, positionSet);

        test.succeed();
    }

    @GameTest(template = "empty_medium")
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

    @GameTest(template = "medium_glass_walls")
    public static void HollowMatchesWorldDefinitionExactly(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forComponents()
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var components = testHelper.components();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        final var blocks = testHelper.blocks();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (!matched) {
            test.fail("Hollow did not pass perfect match.");
        }

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls")
    public static void HollowFailsIfAnyComponentsUnidentified(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forComponents()
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var components = testHelper.components();
        final var blocks = testHelper.blocks();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (matched)
            test.fail("Hollow matched, despite having unidentified, non-air components.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls")
    public static void HollowFailsIfWorldHasBadWallBlock(final GameTestHelper test) {

        final var testHelper = TestHelper.forTest(test)
                .forComponents()
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var components = testHelper.components();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        // register gold block to get past the unknown component early fail
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));

        test.setBlock(BlockPos.ZERO.above(), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = testHelper.blocks();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when BP.ZERO was a different block.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void HollowFailsIfMoreThanOneComponentAndCenterNotEmpty(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forComponents()
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        // we need to register the obsidian block here; the layer will fail early otherwise
        // since otherwise, the center block will be unmatched
        final var components = testHelper.components();
        components.registerBlock("W", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = testHelper.blocks();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("W");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when center block was a different block.");

        test.succeed();
    }
}
