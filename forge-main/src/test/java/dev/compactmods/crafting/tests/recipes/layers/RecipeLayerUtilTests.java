package dev.compactmods.crafting.tests.recipes.layers;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
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
        // We set up a different block in the corner, so we can tell the blocks rotated
        test.setBlock(BlockPos.ZERO.above(), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeComponents components = getRecipeByName(test, "medium_glass_walls_obsidian_center")
                .map(MiniaturizationRecipe::getComponents)
                .orElse(null);

        final var floorBounds = RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test);
        final RecipeBlocks blocks = RecipeBlocks.create(test.getLevel(), components, floorBounds);

        final IRecipeBlocks rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("G")
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        if (originalPositions.equals(rotatedPositions))
            test.fail("Expected rotated block set to not equal original.");

        test.succeed();
    }

    @GameTest(template = "medium_glass_walls_obsidian_center")
    public static void NonRotationCreatesCopiedInstance(final GameTestHelper test) {
        final IRecipeComponents components = getRecipeByName(test, "medium_glass_walls_obsidian_center")
                .map(MiniaturizationRecipe::getComponents)
                .orElse(null);

        final var blocks = RecipeBlocks.create(test.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, test));

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
