package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.recipes.components.CCMiniRecipeComponents;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.tests.world.TestBlockReader;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class FilledLayerTests {

    private FilledComponentRecipeLayer getLayerFromFile(String filename) {
        JsonElement layerJson = FileHelper.INSTANCE.getJsonFromFile(filename);

        return FilledComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow(false, Assertions::fail);
    }

    @Test
    @Tag("minecraft")
    void CanCreateLayerInstance() {
        getLayerFromFile("layers/filled/basic.json");
    }

    @Test
    @Tag("minecraft")
    void ReturnsNoFilledIfDimensionsNull() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");

        // We force the dimensions null here
        layer.setRecipeDimensions((AxisAlignedBB) null);

        int filled = Assertions.assertDoesNotThrow(layer::getNumberFilledPositions);
        Assertions.assertEquals(0, filled);
    }

    @Test
    @Tag("minecraft")
    void CanUpdateDimensions() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");

        int filledBefore = layer.getNumberFilledPositions();

        Assertions.assertDoesNotThrow(() -> layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM));

        int filledAfter = layer.getNumberFilledPositions();

        Assertions.assertNotEquals(filledBefore, filledAfter, "Expected component count to change after growing layer dimensions.");
    }

    @Test
    @Tag("minecraft")
    void ComponentPositionsAreCorrect() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        Assertions.assertEquals(25, layer.getNumberFilledPositions());

        final Set<BlockPos> expected = BlockSpaceUtil.getBlocksIn(MiniaturizationFieldSize.MEDIUM, 0)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> actual = layer.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Tag("minecraft")
    void CanFetchComponentByPosition() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO);
        Assertions.assertTrue(componentForPosition.isPresent());
        componentForPosition.ifPresent(comp -> {
            Assertions.assertEquals("G", comp);
        });
    }

    @Test
    @Tag("minecraft")
    void ReturnsEmptyWhenFetchingOOBPosition() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        // Y = 1 should never happen, in any layer, ever
        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO.above());
        Assertions.assertFalse(componentForPosition.isPresent());
    }

    @Test
    @Tag("minecraft")
    void FilledLayerMatchesWorldInExactMatchScenario() {
        final TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/single_layer_medium_filled_G.json");
        final CCMiniRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("worlds/single_layer_medium_filled_G.json");

        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertTrue(matched);
    }

    @Test
    @Tag("minecraft")
    void FailsMatchIfAllBlocksNotIdentified() {
        final TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_mixed_medium_iron.json");
        final CCMiniRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("worlds/basic_mixed_medium_iron.json");

        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched);
    }

    @Test
    @Tag("minecraft")
    void FailsMatchIfMoreThanOneBlockFound() {
        final TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_harness_allmatched_5x.json");
        final CCMiniRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("worlds/basic_harness_allmatched_5x.json");

        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched);
    }

    @Test
    @Tag("minecraft")
    void FailsMatchIfComponentKeyNotFound() {
        final TestBlockReader reader = RecipeTestUtil.getBlockReader("worlds/basic_filled_medium_glass_A.json");
        final CCMiniRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("worlds/basic_filled_medium_glass_A.json");

        Assertions.assertNotNull(reader);

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(reader, reader.source, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched);
    }
}
