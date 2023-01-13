package dev.compactmods.crafting.tests.testers;

import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.AABB;

public class ComponentTestHelper {

    private final GameTestHelper testHelper;
    private final IRecipeComponents components;

    ComponentTestHelper(GameTestHelper testHelper, IRecipeComponents components) {
        this.testHelper = testHelper;
        this.components = components;
    }

    public SingleLayerComponentTestHelper forSingleLayer(MiniaturizationFieldSize fieldSize) {
        return forSizedLayer(fieldSize, 0);
    }

    public SingleLayerComponentTestHelper forSizedLayer(MiniaturizationFieldSize fieldSize, int layer) {
        final var fieldBounds = ITestableAreaHelper.getFieldBoundsInternal(fieldSize, testHelper.absolutePos(BlockPos.ZERO).above());
        final var layerBounds = BlockSpaceUtil.getLayerBounds(fieldBounds, layer);
        return new SingleLayerComponentTestHelper(testHelper, components, layerBounds);
    }

    public SingleLayerComponentTestHelper forBounds(AABB layerBounds) {
        return new SingleLayerComponentTestHelper(testHelper, components, layerBounds);
    }
}
