package dev.compactmods.crafting.test.gametests.util;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

public class CompactGameTestHelper extends GameTestHelper {

    public CompactGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    public AABB getFieldBounds(MiniaturizationFieldSize fieldSize) {
        var testOrigin = this.absolutePos(BlockPos.ZERO).above();
        var bounds = fieldSize.getBoundsAtOrigin(testOrigin.getY());
        return bounds.move(testOrigin.getX(), 0, testOrigin.getZ());
    }

    public AABB getFloorLayerBounds(MiniaturizationFieldSize fieldSize) {
        return BlockSpaceUtil.getLayerBounds(getFieldBounds(fieldSize), 0);
    }

    public void loadStructureIntoTestArea(ResourceLocation structure, BlockPos location) {
        final var structures = testInfo.getLevel().getStructureManager();
        final var ender = structures.get(structure);
        if(ender.isEmpty())
            return;

        var placeAt = absolutePos(BlockPos.ZERO).offset(location);
        ender.get().placeInWorld(
                testInfo.getLevel(),
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
               testInfo.getLevel().getRandom(),
                Block.UPDATE_ALL);
    }
}
