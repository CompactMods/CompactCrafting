package dev.compactmods.crafting.test.gametests.recipes.layers;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.test.gametests.TestFrameworkTemplates;
import dev.compactmods.crafting.test.testers.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "layers")
public class EmptyLayerTests {

    @GameTest @EmptyTemplate
    public static void CanCreateLayerInstance(final GameTestHelper test) {
        final var layer = new EmptyRecipeLayer();
        test.succeed();
    }

    @GameTest
    @EmptyTemplate(TestFrameworkTemplates.FIVE_CUBED)
    public static void fails_match_if_any_blocks_present(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forComponents()
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var emptyLayer = new EmptyRecipeLayer();
        emptyLayer.setRecipeDimensions(MiniaturizationFieldSize.MEDIUM);

        // Set a block in the field area
        test.setBlock(BlockPos.ZERO.above(), Blocks.GOLD_BLOCK);

        boolean matched = emptyLayer.matches(testHelper.components(), testHelper.blocks());
        if(matched) {
            test.fail("Layer should not have matched.");
        } else {
            test.succeed();
        }
    }
}