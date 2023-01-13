package dev.compactmods.crafting.tests.recipes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class RecipeLevelHelper {

    public static void loadStructureIntoTestArea(GameTestHelper helper, ResourceLocation structure, BlockPos location) {
        final var level = helper.getLevel();
        final var structures = level.getStructureManager();
        final var template = structures.get(structure);
        if(template.isEmpty())
            return;

        final var actualTemplate = template.get();

        var placeAt = helper.absolutePos(BlockPos.ZERO).offset(location);
        actualTemplate.placeInWorld(
                level,
                placeAt,
                placeAt,
                new StructurePlaceSettings(),
                level.getRandom(),
                Block.UPDATE_ALL);
    }

}
