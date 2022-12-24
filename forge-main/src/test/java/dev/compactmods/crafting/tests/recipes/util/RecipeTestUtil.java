package dev.compactmods.crafting.tests.recipes.util;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.tests.util.FileHelper;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class RecipeTestUtil {
    @Nullable
    public static MiniaturizationRecipe getRecipeFromFile(String filename) {
        JsonElement json = FileHelper.getJsonFromFile(filename);

        Optional<MiniaturizationRecipe> loaded = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(CompactCrafting.LOGGER::info);

        if (loaded.isEmpty()) {
            return null;
        } else {
            final MiniaturizationRecipe rec = loaded.get();
            rec.setId(new ResourceLocation("compactcrafting", Files.getNameWithoutExtension(filename)));
            return rec;
        }
    }

    @Nonnull
    public static Optional<MiniaturizationRecipe> getRecipeByName(GameTestHelper helper, String name) {
        return helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .map(r -> (MiniaturizationRecipe) r);
    }

    @Nonnull
    public static Optional<IRecipeComponents> getComponentsFromRecipe(GameTestHelper helper, String name) {
        return getRecipeByName(helper, name).map(MiniaturizationRecipe::getComponents);
    }

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
