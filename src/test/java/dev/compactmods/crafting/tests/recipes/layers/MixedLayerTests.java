package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.components.IRecipeComponent;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.EmptyBlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@IntegrationTestClass("layers")
public class MixedLayerTests {

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);
    }

    static MixedComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile(filename);

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        return parsed.getOrThrow(false, Assertions::fail);
    }

    @Test
    @Tag("minecraft")
    void CanCreateLayerInstanceManually() {
        MixedComponentRecipeLayer layer = new MixedComponentRecipeLayer();
        Assertions.assertNotNull(layer);

        // Dimensions - ensure zero on all dimensions
        final AxisAlignedBB dimensions = Assertions.assertDoesNotThrow(layer::getDimensions);
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
    @Tag("minecraft")
    void CanCreateLayerInstance() {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile("layers/mixed/basic.json");

        DataResult<MixedComponentRecipeLayer> parsed = MixedComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson);
        parsed.resultOrPartial(Assertions::fail)
                .ifPresent(layer -> {
                    Assertions.assertNotNull(layer);
                    int filled = layer.getNumberFilledPositions();

                    Assertions.assertEquals(25, filled);
                });
    }

    @Test
    @Tag("minecraft")
    void MixedLayerRemovesUnknownComponents() {
        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");
        Assertions.assertNotNull(layer);

        // Layer has [G, I, O, -] defined in spec - we want to include all but -
        final MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("data/compactcrafting/recipes/basic_mixed_medium_iron.json");

        layer.dropNonRequiredComponents(components);

        final Set<String> newList = Assertions.assertDoesNotThrow(layer::getComponents);

        Assertions.assertFalse(newList.contains("-"), "Mixed should have removed unmapped - component.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void MixedLayerMatchesWorldInExactMatchScenario(IntegrationTestHelper helper) {

        final MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("data/compactcrafting/recipes/medium_glass_walls_obsidian_center.json");

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");
        layer.dropNonRequiredComponents(components);

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertTrue(matched, "Expected layer to match; layer did not match.");
    }

    @Test
    @Tag("minecraft")
    void MixedCanFetchAKnownGoodPosition() {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");
        Assertions.assertNotNull(layer);

        Optional<String> spot = layer.getComponentForPosition(BlockPos.ZERO);
        Assertions.assertTrue(spot.isPresent());
        Assertions.assertEquals("I", spot.get());
    }

    @Test
    @Tag("minecraft")
    void MixedCanFetchAListOfComponentPositions() {
        MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");
        Assertions.assertNotNull(layer);

        final Stream<BlockPos> g = layer.getPositionsForComponent("G");
        Assertions.assertNotNull(g);

        final Set<BlockPos> positions = g.map(BlockPos::immutable).collect(Collectors.toSet());
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertEquals(15, positions.size());
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void MixedLayerDeniesMatchIfAllComponentsNotIdentified(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("data/compactcrafting/recipes/basic_mixed_medium_iron.json");

        helper.setBlockState(BlockPos.ZERO, Blocks.IRON_BLOCK.defaultBlockState());

        // Force that the - component is unregistered; in a real scenario the recipe system would have remapped it
        // to an empty component due to it existing in the layer spec. Here, we're testing if a legit component in the world
        // did not match.
        components.unregisterBlock("-");

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched, "Expected layer not to match; layer matched anyway.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_filled")
    void MixedLayerDeniesMatchIfComponentCountDiffers(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final Map<String, IRecipeComponent> allComponents = components.getAllComponents();
        final int worldCompCount = allComponents.keySet().size();

        final Set<String> layerComponents = layer.getComponents();
        final int layerCompCount = layerComponents.size();

        Assertions.assertNotEquals(layerCompCount, worldCompCount);

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched, "Expected layer not to match due to component count mismatch; layer matched anyway.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void MixedLayerDeniesMatchIfRequiredComponentsMissing(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("Ob", new BlockComponent(Blocks.OBSIDIAN));

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched, "Expected layer not to match due to missing required components; layer matched anyway.");
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void MixedLayerDeniesMatchIfComponentsInWrongPositions(IntegrationTestHelper helper) {
        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/medium_glass_walls_obsidian_center.json");
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("-", new EmptyBlockComponent());

        // Swap center and a corner block so the components are right but positions are wrong
        helper.setBlockState(new BlockPos(1, 0, 1), Blocks.OBSIDIAN.defaultBlockState());
        helper.setBlockState(new BlockPos(2, 0, 2), Blocks.AIR.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched, "Expected layer not to match due to incorrect positions; layer matched anyway.");
    }
}
