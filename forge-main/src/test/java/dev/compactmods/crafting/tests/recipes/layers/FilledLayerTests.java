package dev.compactmods.crafting.tests.recipes.layers;

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
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class FilledLayerTests {

    private static FilledComponentRecipeLayer getLayerFromFile(GameTestHelper test, String filename) {
        JsonElement layerJson = FileHelper.getJsonFromFile(filename);

        return FilledComponentRecipeLayer.CODEC.parse(JsonOps.INSTANCE, layerJson)
                .getOrThrow(false, test::fail);
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreateLayerInstance(final GameTestHelper test) {
        getLayerFromFile(test, "layers/filled/basic.json");
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void null_dimensions_have_zero_blocks_filled(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test,"layers/filled/basic.json");

        // We force the dimensions null here
        layer.setRecipeDimensions((AABB) null);

        int filled = GameTestAssertions.assertDoesNotThrow(layer::getNumberFilledPositions);
        GameTestAssertions.assertEquals(0, filled);

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanUpdateDimensions(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");

        int filledBefore = layer.getNumberFilledPositions();

        GameTestAssertions.assertDoesNotThrow(() -> layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM));

        int filledAfter = layer.getNumberFilledPositions();

        if(Objects.equals(filledBefore, filledAfter))
            test.fail("Expected component count to change after growing layer dimensions.");

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void ComponentPositionsAreCorrect(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        GameTestAssertions.assertEquals(25, layer.getNumberFilledPositions());

        final Set<BlockPos> expected = BlockSpaceUtil.getBlocksIn(MiniaturizationFieldSize.MEDIUM, 0)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> actual = layer.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        GameTestAssertions.assertEquals(expected, actual);
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanFetchComponentByPosition(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO);
        GameTestAssertions.assertTrue(componentForPosition.isPresent());
        componentForPosition.ifPresent(comp -> {
            GameTestAssertions.assertEquals("G", comp);
        });

        test.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void oob_position_returns_empty(final GameTestHelper test) {
        final FilledComponentRecipeLayer layer = getLayerFromFile(test, "layers/filled/basic.json");
        layer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        // Y = 1 should never happen, in any layer, ever
        final Optional<String> componentForPosition = layer.getComponentForPosition(BlockPos.ZERO.above());
        GameTestAssertions.assertFalse(componentForPosition.isPresent());

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
