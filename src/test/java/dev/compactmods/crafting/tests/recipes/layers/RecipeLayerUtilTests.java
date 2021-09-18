package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Set;
import java.util.stream.Collectors;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.recipes.layers.RecipeLayerUtil;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("recipes")
public class RecipeLayerUtilTests {

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void CanRotate(IntegrationTestHelper helper) {
        // We set up a different block in the corner so we can tell the blocks rotated
        helper.setBlockState(BlockPos.ZERO, Blocks.GOLD_BLOCK.defaultBlockState());

        final MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("recipes/basic_mixed_medium_iron.json");

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper));

        final IRecipeBlocks rotatedClockwise = RecipeLayerUtil.rotate(blocks, Rotation.CLOCKWISE_90);
        Assertions.assertNotNull(rotatedClockwise);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedClockwise.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertNotEquals(originalPositions, rotatedPositions);
    }

    @Tag("minecraft")
    @IntegrationTest("medium_glass_walls_obsidian_center")
    void NonRotationCreatesCopiedInstance(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("recipes/basic_mixed_medium_iron.json");

        final RecipeBlocks blocks = RecipeBlocks.create(helper.getWorld(), components, BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0));

        final IRecipeBlocks rotatedHarness = RecipeLayerUtil.rotate(blocks, Rotation.NONE);
        Assertions.assertNotNull(rotatedHarness);

        final Set<BlockPos> originalPositions = blocks.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());
        final Set<BlockPos> rotatedPositions = rotatedHarness.getPositionsForComponent("G").map(BlockPos::immutable).collect(Collectors.toSet());

        Assertions.assertEquals(originalPositions, rotatedPositions);

        Assertions.assertNotSame(blocks, rotatedHarness);
    }


}
