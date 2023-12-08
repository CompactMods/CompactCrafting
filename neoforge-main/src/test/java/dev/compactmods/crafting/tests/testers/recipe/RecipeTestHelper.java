package dev.compactmods.crafting.tests.testers.recipe;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.tests.testers.TestHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import static dev.compactmods.crafting.tests.testers.ITestableAreaHelper.getFieldBoundsInternal;

public class RecipeTestHelper extends TestHelper implements IRecipeTester {

    private final IMiniaturizationRecipe recipe;

    public RecipeTestHelper(GameTestHelper testHelper, IMiniaturizationRecipe recipe) {
        super(testHelper);
        this.recipe = recipe;
    }

    public MultiLayerRecipeTestHelper forFieldOfSize(MiniaturizationFieldSize fieldSize) {
        return MultiLayerRecipeTestHelper.forFieldSize(testHelper, recipe, fieldSize);
    }

    public SingleLayerRecipeTestHelper forSingleLayerOfSize(MiniaturizationFieldSize fieldSize) {
        return SingleLayerRecipeTestHelper.forSizedLayer(testHelper, recipe, fieldSize, 0);
    }

    public SingleLayerRecipeTestHelper forSingleLayerOfSize(MiniaturizationFieldSize fieldSize, int layer) {
        final var fieldBounds = getFieldBoundsInternal(fieldSize, testHelper.absolutePos(BlockPos.ZERO).above());
        final var layerBounds = BlockSpaceUtil.getLayerBounds(fieldBounds, layer);
        return SingleLayerRecipeTestHelper.forBounds(testHelper, recipe, layerBounds);
    }

    @Override
    public IMiniaturizationRecipe recipe() {
        return this.recipe;
    }
}