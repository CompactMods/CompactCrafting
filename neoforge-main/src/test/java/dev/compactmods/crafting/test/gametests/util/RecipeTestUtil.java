package dev.compactmods.crafting.test.gametests.util;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

public class RecipeTestUtil {

    public static AABB getFieldBounds(MiniaturizationFieldSize fieldSize, GameTestHelper helper) {
        var testOrigin = helper.absolutePos(BlockPos.ZERO).above();
        var bounds = fieldSize.getBoundsAtOrigin(testOrigin.getY());
        return bounds.move(testOrigin.getX(), 0, testOrigin.getZ());
    }

    public static AABB getFloorLayerBounds(MiniaturizationFieldSize fieldSize, GameTestHelper helper) {
        return BlockSpaceUtil.getLayerBounds(getFieldBounds(fieldSize, helper), 0);
    }

    public static void loadStructureIntoTestArea(GameTestHelper test, ResourceLocation structure, BlockPos location) {
        final var structures = test.getLevel().getStructureManager();
        final var ender = structures.get(structure);
        if(ender.isEmpty())
            return;

        var placeAt = test.absolutePos(BlockPos.ZERO).offset(location);
        ender.get().placeInWorld(
                test.getLevel(),
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
                test.getLevel().getRandom(),
                Block.UPDATE_ALL);
    }
}
