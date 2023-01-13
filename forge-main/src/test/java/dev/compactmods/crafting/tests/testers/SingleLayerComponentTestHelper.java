package dev.compactmods.crafting.tests.testers;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class SingleLayerComponentTestHelper implements ITestableAreaHelper, IComponentTester {
    private final GameTestHelper testHelper;
    private final IRecipeComponents components;
    private final AABB layerBounds;

    public SingleLayerComponentTestHelper(GameTestHelper testHelper, IRecipeComponents components, AABB layerBounds) {
        this.testHelper = testHelper;
        this.components = components;
        this.layerBounds = layerBounds;
    }

    @Override
    public IRecipeComponents components() {
        return this.components;
    }

    @Override
    public @NotNull Level getLevel() {
        return testHelper.getLevel();
    }

    @Override
    public @NotNull AABB getTestBounds() {
        return layerBounds;
    }
}
