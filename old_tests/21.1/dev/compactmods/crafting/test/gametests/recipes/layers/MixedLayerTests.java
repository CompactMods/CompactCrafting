package dev.compactmods.crafting.test.gametests.recipes.layers;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.test.FileHelper;
import dev.compactmods.crafting.test.gametests.GameTestAssertions;
import dev.compactmods.crafting.test.gametests.CMTestStructures;
import dev.compactmods.crafting.test.gametests.util.CompactGameTestHelper;
import dev.compactmods.crafting.test.testers.TestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ForEachTest(groups = "layers")
public class MixedLayerTests {

    static MixedComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC
                .codec()
                .parse(JsonOps.INSTANCE, layerJson);

        return parsed.getOrThrow();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void can_create_layer_instance_manually(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = new MixedComponentRecipeLayer();

        // Dimensions - ensure zero on all dimensions
        final var dimensions = layer.getDimensions();

        if(dimensions.getXsize() != 0) test.fail("X Dimensions were not correct.");
        if(dimensions.getYsize() != 0) test.fail("Y Dimensions were not correct.");
        if(dimensions.getZsize() != 0) test.fail("Z Dimensions were not correct.");

        // Components - must be created and empty
        final var lookup = layer.getComponentLookup();
        test.assertTrue(lookup != null, "Lookup should not be null.");

        final Collection<String> componentKeys = lookup.getComponents();
        test.assertTrue(componentKeys.isEmpty(), "component keys should be empty");

        final Stream<BlockPos> positions = lookup.getAllPositions();
        test.assertTrue(positions != null, "Positions should not be null");
        if(positions.findAny().isPresent())
            test.fail("Expected no positions on fresh instance.");

        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void can_create_mixed_layer(final GameTestHelper test) {
        JsonElement layerJson = FileHelper.getJsonFromFile("layers/mixed/basic.json");

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC
                .codec()
                .parse(JsonOps.INSTANCE, layerJson);

        parsed.resultOrPartial(test::fail)
                .ifPresent(layer -> {
                    int filled = layer.getNumberFilledPositions();

                    GameTestAssertions.assertEquals(25, filled);
                });

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public static void MixedLayerMatchesWorldInExactMatchScenario(final GameTestHelper test) {

        final var testHelper = TestHelper.forTest(test)
                .forComponents("components/glass_and_obsidian.json")
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var components = testHelper.components();
        final var blocks = testHelper.blocks();

        final var layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");

        // This would happen in a normal recipe
        layer.getComponentLookup().remove("-");

        final var matched = layer.matches(components, blocks);
        if(!matched)
            test.fail("Expected layer to match; layer did not match.");

        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void MixedCanFetchAKnownGoodPosition(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

        Optional<String> spot = layer.getComponentForPosition(BlockPos.ZERO);
        test.assertTrue(spot.isPresent(), "Component not found.");
        GameTestAssertions.assertEquals("I", spot.get());

        test.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate(CMTestStructures.ONE_CUBED)
    public static void mixed_component_positions(final GameTestHelper test) {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

        final Stream<BlockPos> g = layer.getPositionsForComponent("G");
        GameTestAssertions.assertNotNull(g);

        final Set<BlockPos> positions = g.map(BlockPos::immutable).collect(Collectors.toSet());
        test.assertFalse(positions.isEmpty(), "Positions matched.");
        GameTestAssertions.assertEquals(15, positions.size());

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
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

        final var layer = getLayerFromFile("layers/mixed/basic.json");

        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match; layer matched anyway.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_FILLED)
    public static void MixedLayerDeniesMatchIfComponentCountDiffers(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

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

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public static void MixedLayerDeniesMatchIfRequiredComponentsMissing(final CompactGameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("Ob", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM))
                .normalize();

        final var layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");

        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to missing required components; layer matched anyway.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public static void MixedLayerDeniesMatchIfComponentsInWrongPositions(final CompactGameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("-", new EmptyBlockComponent());

        // Swap center and a corner block so the components are right but positions are wrong
        test.setBlock(new BlockPos(1, 1, 1), Blocks.OBSIDIAN.defaultBlockState());
        test.setBlock(new BlockPos(2, 1, 2), Blocks.AIR.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, test.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");
        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to incorrect positions; layer matched anyway.");

        test.succeed();
    }
}
