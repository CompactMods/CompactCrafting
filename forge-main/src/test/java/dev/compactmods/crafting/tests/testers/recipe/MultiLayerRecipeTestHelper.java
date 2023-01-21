package dev.compactmods.crafting.tests.testers.recipe;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.tests.testers.component.IComponentTester;
import dev.compactmods.crafting.tests.testers.ITestHelper;
import dev.compactmods.crafting.tests.testers.ITestableAreaHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MultiLayerRecipeTestHelper implements ITestableAreaHelper, IRecipeTester, ITestHelper, IComponentTester {

    private final GameTestHelper helper;
    private final AABB localTestBounds;
    private final IMiniaturizationRecipe recipe;

    private MultiLayerRecipeTestHelper(GameTestHelper testHelper, IMiniaturizationRecipe recipe, AABB localBounds) {
        this.recipe = recipe;
        this.helper = testHelper;
        this.localTestBounds = localBounds;
    }

    public static MultiLayerRecipeTestHelper forFieldSize(GameTestHelper test, IMiniaturizationRecipe recipe, MiniaturizationFieldSize fieldSize) {
        final var fieldBounds = ITestableAreaHelper.getFieldBoundsInternal(fieldSize, test.absolutePos(BlockPos.ZERO).above());
        return new MultiLayerRecipeTestHelper(test, recipe, fieldBounds);
    }

    public static MultiLayerRecipeTestHelper forTest(GameTestHelper test, IMiniaturizationRecipe recipe) {
        return new MultiLayerRecipeTestHelper(test, recipe, AABB.unitCubeFromLowerCorner(Vec3.ZERO));
    }

//    @Nonnull
//    public Optional<IRecipeComponents> getComponentsFromRecipe(String name) {
//        return getRecipeByName(name).map(MiniaturizationRecipe::getComponents);
//    }

    public AABB getFieldBounds(MiniaturizationFieldSize fieldSize) {
        return getFieldBounds(fieldSize, BlockPos.ZERO);
    }

    public AABB getFieldBounds(MiniaturizationFieldSize fieldSize, BlockPos relative) {
        var testOrigin = helper.absolutePos(relative).above();
        return ITestableAreaHelper.getFieldBoundsInternal(fieldSize, testOrigin);
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
    public IMiniaturizationRecipe recipe() {
        return this.recipe;
    }

    @Override
    public IRecipeComponents components() {
        return recipe.getComponents();
    }

    public AABB getLayerBounds(int layer) {
        final var relativeOffset = BlockPos.ZERO.subtract(this.helper.absolutePos(BlockPos.ZERO.above()));
        return BlockSpaceUtil.getLayerBounds(this.localTestBounds, layer).move(relativeOffset);
    }
}