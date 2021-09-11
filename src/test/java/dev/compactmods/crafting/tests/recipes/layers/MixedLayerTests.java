package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.ComponentPositionLookup;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import dev.compactmods.crafting.recipes.layers.MixedComponentRecipeLayer;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.tests.world.TestBlockReader;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MixedLayerTests {

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
    void remapsUnknownComponents() {
        MiniaturizationRecipe recipe = RecipeTestUtil.getRecipeFromFile("recipes/ender_crystal.json");
        if(recipe == null)
            Assertions.fail();

        Optional<IRecipeLayer> layer = recipe.getLayer(2);
        layer.ifPresent(lay -> {
            // "-" is a component that was not found in the recipe file's component list, so it needs remapped
            Map<String, Integer> totals = lay.getComponentTotals();
            Assertions.assertTrue(totals.containsKey("-"));
        });

        // Now check if the component was remapped
        Set<String> components = recipe.getComponents().getAllComponents().keySet();
        Assertions.assertTrue(components.contains("-"));
    }

    @Test
    @Tag("minecraft")
    void MixedLayerMatchesWorldInExactMatchScenario() {
        final TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_harness_5x.json");
        final CCMiniRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("worlds/basic_harness_5x.json");

        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        RecipeTestUtil.remapUnknownToAir(blocks, components);

        final MixedComponentRecipeLayer layer = getLayerFromFile("layers/mixed/basic.json");

        final Boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertTrue(matched, "Expected layer to match; layer did not match.");
    }
}
