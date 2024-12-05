package dev.compactmods.crafting.test.gametests.recipes;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.test.gametests.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.testframework.annotation.ForEachTest;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ForEachTest(groups = "miniaturization_recipes")
public class MiniaturizationRecipeTests {

    @Nullable
    private static MiniaturizationRecipe getRecipe(GameTestHelper testHelper, String name) {
        return (MiniaturizationRecipe) testHelper.getLevel()
                .getRecipeManager()
                .byKey(CompactCrafting.modRL(name))
                .map(RecipeHolder::value)
                .orElse(null);
    }

    @GameTest(template = "recipes/ender_crystal")
    public void MatchesExactStructure(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        try {
            boolean matched = enderCrystal.matches(blocks);
            if(!matched) {
                test.fail("Recipe should have matched.");
            }

            test.succeed();
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }
    }

    @GameTest(template = "recipes/ender_crystal")
    public void RecipeFailsIfUnidentifiedBlock(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        // Force an unknown component in the exact center
        test.setBlock(new BlockPos(2, 2, 2), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        try {
            boolean matched = enderCrystal.matches(blocks);
            if(matched)
                test.fail("Recipe did not fail the matching process.");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "recipes/ender_crystal")
    public void RecipeFailsIfDifferentDimensions(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "compact_walls");
        Objects.requireNonNull(recipe);

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = recipe.matches(blocks);
        if(matched)
            test.fail("Recipe matched even though dimensions are different.");

        test.succeed();
    }

    @GameTest(template = "recipes/empty_medium")
    public void RecipeFailsIfNoRotationsMatched(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(recipe);

        // Set up the 8 corners to be glass, so block creation below matches field boundaries
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockSpaceUtil.getCornersOfBounds(MiniaturizationFieldSize.MEDIUM).forEach(p -> test.setBlock(p, glass));

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = recipe.matches(blocks);
        if(matched)
            test.fail("Recipe matched even though blocks are different. (Spatial dimensions equal.)");

        test.succeed();
    }
}
