package dev.compactmods.crafting.tests.recipes.layers;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MixedLayerTests {

    static MixedComponentRecipeLayer getLayerFromFile(GameTestHelper test, String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        return parsed.getOrThrow(false, test::fail);
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void can_create_layer_instance_manually(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = new MixedComponentRecipeLayer();
        GameTestAssertions.assertNotNull(layer);

        // Dimensions - ensure zero on all dimensions
        final var dimensions = layer.getDimensions();

        GameTestAssertions.assertNotNull(dimensions);
        if(dimensions.getXsize() != 0) test.fail("X Dimensions were not correct.");
        if(dimensions.getYsize() != 0) test.fail("Y Dimensions were not correct.");
        if(dimensions.getZsize() != 0) test.fail("Z Dimensions were not correct.");

        // Components - must be created and empty
        final ComponentPositionLookup lookup = GameTestAssertions.assertDoesNotThrow(layer::getComponentLookup);
        GameTestAssertions.assertNotNull(lookup);

        final Collection<String> componentKeys = GameTestAssertions.assertDoesNotThrow(lookup::getComponents);
        GameTestAssertions.assertTrue(componentKeys.isEmpty());

        final Stream<BlockPos> positions = GameTestAssertions.assertDoesNotThrow(lookup::getAllPositions);
        GameTestAssertions.assertNotNull(positions);
        if(positions.findAny().isPresent())
            test.fail("Expected no positions on fresh instance.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void can_create_mixed_layer(final GameTestHelper test) {
        JsonElement layerJson = FileHelper.getJsonFromFile("layers/mixed/basic.json");

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        parsed.resultOrPartial(test::fail)
                .ifPresent(layer -> {
                    GameTestAssertions.assertNotNull(layer);
                    int filled = layer.getNumberFilledPositions();

                    GameTestAssertions.assertEquals(25, filled);
                });

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void MixedLayerRemovesUnknownComponents(final GameTestHelper test) {
        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/basic.json");
        Objects.requireNonNull(layer);

        // Layer has [G, I, O, -] defined in spec - we want to include all but -
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("I", new BlockComponent(Blocks.IRON_BLOCK));

        layer.dropNonRequiredComponents(components);

        final Set<String> newList = layer.getComponents();

        if(newList.contains("-"))
            test.fail("Mixed should have removed unmapped - component.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void MixedLayerMatchesWorldInExactMatchScenario(final GameTestHelper test) {

        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/medium_glass_walls_obsidian_center.json");
        layer.dropNonRequiredComponents(components);

        final var matched = layer.matches(components, blocks);
        if(!matched)
            test.fail("Expected layer to match; layer did not match.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void MixedCanFetchAKnownGoodPosition(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/basic.json");
        GameTestAssertions.assertNotNull(layer);

        Optional<String> spot = layer.getComponentForPosition(BlockPos.ZERO);
        GameTestAssertions.assertTrue(spot.isPresent());
        GameTestAssertions.assertEquals("I", spot.get());

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void mixed_component_positions(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/basic.json");
        GameTestAssertions.assertNotNull(layer);

        final Stream<BlockPos> g = layer.getPositionsForComponent("G");
        GameTestAssertions.assertNotNull(g);

        final Set<BlockPos> positions = g.map(BlockPos::immutable).collect(Collectors.toSet());
        GameTestAssertions.assertFalse(positions.isEmpty());
        GameTestAssertions.assertEquals(15, positions.size());

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void MixedLayerDeniesMatchIfAllComponentsNotIdentified(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("I", new BlockComponent(Blocks.IRON_BLOCK));

        test.setBlock(BlockPos.ZERO, Blocks.IRON_BLOCK.defaultBlockState());

        // Force that the - component is unregistered; in a real scenario the recipe system would have remapped it
        // to an empty component due to it existing in the layer spec. Here, we're testing if a legit component in the world
        // did not match.
        components.unregisterBlock("-");

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final var layer = getLayerFromFile(test, "layers/mixed/basic.json");

        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match; layer matched anyway.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_filled")
    public static void MixedLayerDeniesMatchIfComponentCountDiffers(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/basic.json");

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final Map<String, IRecipeComponent> allComponents = components.getAllComponents();
        final int worldCompCount = allComponents.keySet().size();

        final Set<String> layerComponents = layer.getComponents();
        final int layerCompCount = layerComponents.size();

        if(layerCompCount == worldCompCount)
            test.fail("Layer totals should not have matched");

        final boolean matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to component count mismatch; layer matched anyway.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void MixedLayerDeniesMatchIfRequiredComponentsMissing(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("Ob", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        // Layer has "G" and "O" components
        final var layer = getLayerFromFile(test, "layers/mixed/medium_glass_walls_obsidian_center.json");

        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to missing required components; layer matched anyway.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void MixedLayerDeniesMatchIfComponentsInWrongPositions(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("-", new EmptyBlockComponent());

        // Swap center and a corner block so the components are right but positions are wrong
        test.setBlock(new BlockPos(1, 1, 1), Blocks.OBSIDIAN.defaultBlockState());
        test.setBlock(new BlockPos(2, 1, 2), Blocks.AIR.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/medium_glass_walls_obsidian_center.json");
        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to incorrect positions; layer matched anyway.");

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void MixedLayerMatchesIfCompletelyEmpty(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/empty.json");
        layer.dropNonRequiredComponents(components);

        final var matched = layer.matches(components, blocks);

        if(!matched)
            test.fail("Empty area did not match correctly.");

        test.succeed();
    }
}
