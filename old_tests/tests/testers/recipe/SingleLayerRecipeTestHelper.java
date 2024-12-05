package dev.compactmods.crafting.tests.testers.recipe;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.tests.testers.component.IComponentTester;
import dev.compactmods.crafting.tests.testers.ITestableAreaHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class SingleLayerRecipeTestHelper implements ITestableAreaHelper, IComponentTester {

    private final GameTestHelper helper;
    private final AABB localTestBounds;

    private final IMiniaturizationRecipe recipe;

    SingleLayerRecipeTestHelper(GameTestHelper testHelper, IMiniaturizationRecipe recipe, AABB localBounds) {
        this.helper = testHelper;
        this.localTestBounds = localBounds;
        this.recipe = recipe;
    }

    public static SingleLayerRecipeTestHelper forSizedLayer(GameTestHelper test, IMiniaturizationRecipe recipe, MiniaturizationFieldSize fieldSize, int layer) {
        final var fieldBounds = ITestableAreaHelper.getFieldBoundsInternal(fieldSize, test.absolutePos(BlockPos.ZERO).above());
        final var layerBounds = BlockSpaceUtil.getLayerBounds(fieldBounds, layer);
        return new SingleLayerRecipeTestHelper(test, recipe, layerBounds);
    }

    public static SingleLayerRecipeTestHelper forBounds(GameTestHelper test, IMiniaturizationRecipe recipe, AABB layerBounds) {
        return new SingleLayerRecipeTestHelper(test, recipe, layerBounds);
    }

    @Override
    public @NotNull Level getLevel() {
        return helper.getLevel();
    }

    @Override
    public @NotNull AABB getTestBounds() {
        return localTestBounds;
    }

    @Override
    public IRecipeComponents components() {
        return this.recipe.getComponents();
    }

    @NotNull
    @Override
    public IRecipeBlocks blocks() {
        final var level = getLevel();
        final var testBounds = getTestBounds();
        return RecipeBlocks.create(level, this.recipe.getComponents(), testBounds).normalize();
    }

    public IMiniaturizationRecipe recipe() {
        return this.recipe;
    }
}