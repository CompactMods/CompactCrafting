package dev.compactmods.crafting.tests.recipes.layers;

import java.util.Set;
import java.util.stream.Collectors;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.blocks.RecipeLayerBlocks;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;

@IntegrationTestClass("recipes")
public class RecipeLayerBlocksTests {

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanCreate(IntegrationTestHelper helper) {
        IRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("recipes/ender_crystal.json");

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(helper.getWorld(), components, MiniaturizationFieldSize.MEDIUM.getBoundsAtOrigin());

        Assertions.assertNotNull(blocks);

        final int compCount = Assertions.assertDoesNotThrow(blocks::getNumberKnownComponents);

        Assertions.assertNotEquals(0, compCount);
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void CanCreateWithUnknownComponents(IntegrationTestHelper helper) {
        // defines G and O as components - "-" should be an unknown position in this recipe
        MiniaturizationRecipeComponents components = RecipeTestUtil.getComponentsFromRecipeFile("recipes/ender_crystal.json");

        final Set<String> keys = components.getBlockComponents().keySet();
        Assertions.assertEquals(2, keys.size());

        final RecipeLayerBlocks blocks = RecipeLayerBlocks.create(helper.getWorld(), components, RecipeTestUtil.getFloorLayerBounds(MiniaturizationFieldSize.MEDIUM, helper));

        final Set<BlockPos> unknownSet = blocks.getUnmappedPositions()
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());

        helper.assertTrue(() -> !unknownSet.isEmpty(), "Expected at least one unmapped position.");
        helper.assertTrue(() -> unknownSet.size() == 8, "Expected the inner-border ring to be empty (8 total).");

        final AxisAlignedBB layerBounds = BlockSpaceUtil.getLayerBounds(MiniaturizationFieldSize.MEDIUM, 0);
        final Set<BlockPos> centerPositions = BlockSpaceUtil.getCenterPositions(layerBounds)
                .map(BlockPos::immutable).collect(Collectors.toSet());

        final Set<BlockPos> testUnknownInnerPositions = BlockSpaceUtil
                .getInnerPositions(layerBounds)
                .map(BlockPos::immutable)
                .filter(ip -> !centerPositions.contains(ip))
                .collect(Collectors.toSet());

        Assertions.assertEquals(testUnknownInnerPositions, unknownSet);
    }
}
