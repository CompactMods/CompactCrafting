package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.tests.testers.TestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MultiLayerRecipeTests {

    @Nullable
    private static MiniaturizationRecipe getRecipe(GameTestHelper testHelper, String name) {
        return (MiniaturizationRecipe) testHelper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .orElse(null);
    }

    @GameTest(template = GameTestTemplates.MEDIUM_FIELD)
    public static void EmptyCornersAreFine(final GameTestHelper test) {
        final var testHelper = TestHelper.forTest(test)
                .forRecipe("empty_corners_5x5")
                .forSingleLayerOfSize(MiniaturizationFieldSize.MEDIUM);

        final var recipe = testHelper.recipe();
        final var blocks = testHelper.blocks();

        // recipe.matches()

        test.succeed();
    }
}
