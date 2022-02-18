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
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import org.junit.jupiter.api.Assertions;

public class RecipeLayerUtilTests {

    @Nonnull
    private static Optional<MiniaturizationRecipe> getRecipeByName(final GameTestHelper helper, String name) {
        return helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .map(r -> (MiniaturizationRecipe) r);
    }


    @GameTest(template = "medium_glass_walls_obsidian_center", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanRotate(final GameTestHelper helper) {
        // We set up a different block in the corner, so we can tell the blocks rotated
        helper.setBlock(BlockPos.ZERO, Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeComponents components = getRecipeByName(helper, "medium_glass_walls_obsidian_center")
                .map(MiniaturizationRecipe::getComponents).orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper));

        final IRecipeBlocks rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertNotEquals(originalPositions, rotatedPositions);
    }


    @GameTest(template = "medium_glass_walls_obsidian_center", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void NonRotationCreatesCopiedInstance(final GameTestHelper helper) {
        final IRecipeComponents components = getRecipeByName(helper, "medium_glass_walls_obsidian_center")
                .map(MiniaturizationRecipe::getComponents).orElse(null);

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getLevel(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final IRecipeBlocks rotatedHarness = RecipeLayerUtil.rotate(blocks, Rotation.NONE);
        Assertions.assertNotNull(rotatedHarness);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedHarness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertEquals(originalPositions, rotatedPositions);

        Assertions.assertNotSame(blocks, rotatedHarness);
    }
}
