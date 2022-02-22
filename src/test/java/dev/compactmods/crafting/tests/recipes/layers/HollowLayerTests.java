package dev.compactmods.crafting.tests.recipes.layers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class HollowLayerTests {

    private HollowComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        return HollowComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow(false, Assertions::fail);
    }

    @Test
    void CanCreateHollowLayerWithConstructor() {
        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        Assertions.assertNotNull(layer);
    }

    @Test
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
    public static void HollowMatchesWorldDefinitionExactly(final GameTestHelper helper) {
        final BlockPos zeroPoint = helper.relativePos(BlockPos.ZERO);

        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        final AABB bounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0).move(zeroPoint);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (!matched) {
            helper.fail("Hollow did not pass perfect match.");
        }
    }


    @GameTest(template = "medium_glass_walls")
    public static void HollowFailsIfAnyComponentsUnidentified(final GameTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        final AABB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper);

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, bounds).normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);

        if (!matched)
            helper.fail("Hollow did not pass perfect match.");

        helper.succeed();
    }


    @DisplayName("Hollow - Bad Wall Block")
    @GameTest(template = "medium_glass_walls")
    public static void HollowFailsIfWorldHasBadWallBlock(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("A", new BlockComponent(Blocks.GLASS));

        // register gold block to get past the unknown component early fail
        components.registerBlock("G", new BlockComponent(Blocks.GOLD_BLOCK));

        test.setBlock(BlockPos.ZERO, Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("A");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when BP.ZERO was a different block.");

        test.succeed();
    }


    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void HollowFailsIfMoreThanOneComponentAndCenterNotEmpty(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("W", new BlockComponent(Blocks.GLASS));

        // we need to register the obsidian block here; the layer will fail early otherwise
        // since otherwise, the center block will be unmatched
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        HollowComponentRecipeLayer layer = new HollowComponentRecipeLayer("W");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final boolean matches = layer.matches(components, blocks);

        if (matches)
            test.fail("Hollow matched when center block was a different block.");

        test.succeed();
    }
}
