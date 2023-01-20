package dev.compactmods.crafting.tests.recipes.layers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.layers.EmptyRecipeLayer;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.testers.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class EmptyLayerTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreateLayerInstance(final GameTestHelper test) {
        final var layer = new EmptyRecipeLayer();
        test.succeed();
    }

    @GameTest(template = GameTestTemplates.MEDIUM_FIELD)
    public static void failsMatchIfAnyBlocksPresent(final GameTestHelper test) {
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
