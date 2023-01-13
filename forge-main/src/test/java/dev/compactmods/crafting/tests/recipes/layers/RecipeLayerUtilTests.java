package dev.compactmods.crafting.tests.recipes.layers;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.tests.testers.MultiLayerRecipeTestHelper;
import dev.compactmods.crafting.tests.testers.TestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class RecipeLayerUtilTests {

    @Nonnull
    private static Optional<MiniaturizationRecipe> getRecipeByName(final GameTestHelper helper, String name) {
        return helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .map(r -> (MiniaturizationRecipe) r);
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void CanRotate(final GameTestHelper test) {


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

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void NonRotationCreatesCopiedInstance(final GameTestHelper test) {
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
