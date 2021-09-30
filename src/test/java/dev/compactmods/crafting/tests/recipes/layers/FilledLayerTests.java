package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
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
    void FilledFailsMatchWithEmptyBlockList() {
        final FilledComponentRecipeLayer layer = getLayerFromFile("layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        RecipeBlocks blocks = RecipeBlocks.createEmpty();
        Assertions.assertFalse(layer.matches(components, blocks));
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_filled")
    void FilledLayerMatchesWorldInExactMatchScenario(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        final AxisAlignedBB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final IRecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, bounds).normalize();

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertTrue(matched);
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void FailsMatchIfAllBlocksNotIdentified(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("data/compactcrafting/recipes/basic_mixed_medium_iron.json");

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched);
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void FailsMatchIfMoreThanOneBlockFound(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        Assertions.assertFalse(matched);
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_filled")
    void FailsMatchIfComponentKeyNotFound(IntegrationTestHelper helper) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        /*
         We actually expect 'G' not 'Gl', so this will match,
         but it will not be what the layer is looking for
        */
        components.registerBlock("Gl", new BlockComponent(Blocks.GLASS));

        final AxisAlignedBB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper);
        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, bounds);

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = Assertions.assertDoesNotThrow(() -> layer.matches(components, blocks));
        helper.assertTrue(() -> !matched, "Layer matched despite not having the required component defined.");
    }
}
