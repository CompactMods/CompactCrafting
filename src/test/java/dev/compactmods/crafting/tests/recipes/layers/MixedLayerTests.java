package dev.compactmods.crafting.tests.recipes.layers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MixedLayerTests {

    static MixedComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        return parsed.getOrThrow(false, Assertions::fail);
    }

    static MixedComponentRecipeLayer getLayerFromFile(GameTestHelper test, String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        return parsed.getOrThrow(false, test::fail);
    }

    @Test
    void CanCreateLayerInstanceManually() {
        MixedComponentRecipeLayer layer = new MixedComponentRecipeLayer();
        Assertions.assertNotNull(layer);

        // Dimensions - ensure zero on all dimensions
        final var dimensions = layer.getDimensions();

        Assertions.assertNotNull(dimensions);
        Assertions.assertEquals(0, dimensions.getXsize());
        Assertions.assertEquals(0, dimensions.getYsize());
        Assertions.assertEquals(0, dimensions.getZsize());

        // Components - must be created and empty
        final ComponentPositionLookup lookup = Assertions.assertDoesNotThrow(layer::getComponentLookup);
        Assertions.assertNotNull(lookup);
        final Collection<String> componentKeys = Assertions.assertDoesNotThrow(lookup::getComponents);
        Assertions.assertTrue(componentKeys.isEmpty());
        final Stream<BlockPos> positions = Assertions.assertDoesNotThrow(lookup::getAllPositions);
        Assertions.assertNotNull(positions);
        Assertions.assertEquals(0, positions.count());
    }

    @Test
    void CanCreateLayerInstance() {
        JsonElement layerJson = FileHelper.getJsonFromFile("layers/mixed/basic.json");

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        parsed.resultOrPartial(Assertions::fail)
                .ifPresent(layer -> {
                    Assertions.assertNotNull(layer);
                    int filled = layer.getNumberFilledPositions();

                    Assertions.assertEquals(25, filled);
                });
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

    @Test
    void MixedCanFetchAKnownGoodPosition() {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");
        Assertions.assertNotNull(layer);

        Optional<String> spot = layer.getComponentForPosition(BlockPos.ZERO);
        Assertions.assertTrue(spot.isPresent());
        Assertions.assertEquals("I", spot.get());
    }

    @Test
    void MixedCanFetchAListOfComponentPositions() {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");
        Assertions.assertNotNull(layer);

        final Stream<BlockPos> g = layer.getPositionsForComponent("G");
        Assertions.assertNotNull(g);

        final Set<BlockPos> positions = g.map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertEquals(15, positions.size());
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
        test.setBlock(new BlockPos(1, 0, 1), Blocks.OBSIDIAN.defaultBlockState());
        test.setBlock(new BlockPos(2, 0, 2), Blocks.AIR.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile(test, "layers/mixed/medium_glass_walls_obsidian_center.json");
        final var matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Expected layer not to match due to incorrect positions; layer matched anyway.");

        test.succeed();
    }
}
