package dev.compactmods.crafting.test.gametests.recipes.layers;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.FilledComponentRecipeLayer;
import dev.compactmods.crafting.test.FileHelper;
import dev.compactmods.crafting.test.gametests.TestFrameworkTemplates;
import dev.compactmods.crafting.test.gametests.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ForEachTest(groups = "layers")
public class FilledLayerTests {

    private static FilledComponentRecipeLayer getLayerFromFile(GameTestHelper test, String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        return FilledComponentRecipeLayer.CODEC.codec()
                .parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void null_dimensions_have_zero_blocks_filled(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test,"layers/filled/basic.json");

        // We force the dimensions null here
        layer.setRecipeDimensions((AABB) null);

        int filled = layer.getNumberFilledPositions();
        test.assertValueEqual(filled, 0, "filled");

        test.succeed();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void CanUpdateDimensions(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");

        int filledBefore = layer.getNumberFilledPositions();

        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        int filledAfter = layer.getNumberFilledPositions();

        if(Objects.equals(filledBefore, filledAfter))
            test.fail("Expected component count to change after growing layer dimensions.");

        test.succeed();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void ComponentPositionsAreCorrect(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        test.assertValueEqual(layer.getNumberFilledPositions(), 25, "filled positions");

        final Set<BlockPos> expected = BlockSpaceUtil.getBlocksIn(MiniaturizationFieldSize.MEDIUM, 0)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> actual = layer.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        test.assertValueEqual(actual, expected, "actual positions");
        test.succeed();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void CanFetchComponentByPosition(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO);
        test.assertTrue(componentForPosition.isPresent(), "component not present");
        componentForPosition.ifPresent(comp -> {
            test.assertValueEqual(comp, "G", "component");
        });

        test.succeed();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.ONE_CUBED)
    public static void oob_position_returns_empty(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        // Y = 1 should never happen, in any layer, ever
        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO.above());
        test.assertFalse(componentForPosition.isPresent(), "component not present");

        test.succeed();
    }

    // note - we use the empty medium here just to let the gametest system run our test
    // we create an actual blocks instance inside the test
    @GameTest(template = "empty_medium")
    public static void FilledFailsMatchWithEmptyBlockList(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        RecipeBlocks blocks = RecipeBlocks.createEmpty();
        if(layer.matches(components, blocks))
            test.fail("Layer matched with empty block information.");

        test.succeed();
    }


    @GameTest(template = "medium_glass_filled")
    public static void FilledLayerMatchesWorldInExactMatchScenario(GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));

        final AABB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test);
        final IRecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, bounds).normalize();

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        try {
            boolean matched = layer.matches(components, blocks);
            if(!matched)
                test.fail("Layer did not match exact scenario");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }


    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void FailsMatchIfAllBlocksNotIdentified(final GameTestHelper test) {
        final IRecipeComponents components = new MiniaturizationRecipeComponents();
        // note the lack of a G component here, missing "GLASS"
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));
        components.registerBlock("I", new BlockComponent(Blocks.IRON_BLOCK));

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Layer should not have matched.");

        test.succeed();
    }


    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void FailsMatchIfMoreThanOneBlockFound(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();
        components.registerBlock("G", new BlockComponent(Blocks.GLASS));
        components.registerBlock("O", new BlockComponent(Blocks.OBSIDIAN));

        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);
        if(matched)
            test.fail("Layer should not have matched.");

        test.succeed();
    }


    @GameTest(template = "medium_glass_filled")
    public static void FailsMatchIfComponentKeyNotFound(final GameTestHelper test) {
        final MiniaturizationRecipeComponents components = new MiniaturizationRecipeComponents();

        /*
         We actually expect 'G' not 'Gl', so this will match,
         but it will not be what the layer is looking for
        */
        components.registerBlock("Gl", new BlockComponent(Blocks.GLASS));

        final AABB bounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test);
        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, bounds);

        // Set up a 5x5x1 filled layer, using "G" component
        final FilledComponentRecipeLayer layer = new FilledComponentRecipeLayer("G");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        boolean matched = layer.matches(components, blocks);
        if (matched)
            test.fail("Layer matched despite not having the required component defined.");

        test.succeed();
    }
}
