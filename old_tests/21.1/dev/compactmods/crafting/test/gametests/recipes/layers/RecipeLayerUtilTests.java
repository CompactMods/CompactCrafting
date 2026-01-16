package dev.compactmods.crafting.test.gametests.recipes.layers;

import java.util.Set;
import java.util.stream.Collectors;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.test.gametests.CMTestStructures;
import dev.compactmods.crafting.test.testers.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "layers")
public class RecipeLayerUtilTests {

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public void CanRotate(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forComponents("components/glass_and_obsidian.json")
                .forSingleLayer(MiniaturizationFieldSize.MEDIUM);

        final var components = testHelper.components();
        components.registerBlock("Go", new BlockComponent(Blocks.GOLD_BLOCK));

        // We set up a different block in the corner, so we can tell the blocks rotated
        test.setBlock(BlockPos.ZERO.above(), Blocks.GOLD_BLOCK.defaultBlockState());

        // Take snapshot of original block positions (from the world)
        final var blocks = testHelper.blocks();

        // Rotate the in-memory representation of the blocks
        final var rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("Go")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("Go")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if (originalPositions.equals(rotatedPositions))
            test.fail("Expected rotated block set to not equal original.");

        test.succeed();
    }

    @TestHolder
    @GameTest(template = CMTestStructures.MEDIUM_GLASS_WALLS_OBSIDIAN_CENTER)
    public void NonRotationCreatesCopiedInstance(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("medium_glass_walls_obsidian_center")
                .forSingleLayerOfSize(MiniaturizationFieldSize.MEDIUM);

        final var blocks = testHelper.blocks();

        final var rotatedHarness = RecipeLayerUtil.rotate(blocks, Rotation.NONE);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> rotatedPositions = rotatedHarness.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if(!originalPositions.equals(rotatedPositions))
            test.fail("Non-rotation changed block positions.");

        if(blocks == rotatedHarness)
            test.fail("Rotation method did not create new instance.");

        test.succeed();
    }
}
