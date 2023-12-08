package dev.compactmods.crafting.tests.testers;

import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeComponents;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.MiniaturizationRecipeComponents;
import dev.compactmods.crafting.tests.testers.component.ComponentTestHelper;
import dev.compactmods.crafting.tests.testers.recipe.RecipeTestHelper;
import dev.compactmods.crafting.tests.util.FileHelper;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

public class TestHelper implements ITestHelper {

    protected final GameTestHelper testHelper;

    protected TestHelper(GameTestHelper test) {
        this.testHelper = test;
    }

    public static TestHelper forTest(GameTestHelper testHelper) {
        return new TestHelper(testHelper);
    }

    public RecipeTestHelper forRecipe(IMiniaturizationRecipe recipe) {
        return new RecipeTestHelper(testHelper, recipe);
    }

    public RecipeTestHelper forRecipe(ResourceLocation recipeId) {
        final var recipe = testHelper.getLevel().getRecipeManager()
                .byKey(recipeId)
                .map(r -> (MiniaturizationRecipe) r)
                .orElseThrow();

        return new RecipeTestHelper(testHelper, recipe);
    }

    public RecipeTestHelper forRecipe(String recipeId) {
        final var recipe = testHelper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation(CompactCrafting.MOD_ID, recipeId))
                .map(r -> (MiniaturizationRecipe) r)
                .orElseThrow();

        return new RecipeTestHelper(testHelper, recipe);
    }

    public ComponentTestHelper forComponents() {
        return new ComponentTestHelper(this.testHelper, new MiniaturizationRecipeComponents());
    }

    public ComponentTestHelper forComponents(IRecipeComponents components) {
        return new ComponentTestHelper(this.testHelper, components);
    }

    public ComponentTestHelper forComponents(String componentFilename) {
        final var json = FileHelper.getJsonFromFile(componentFilename);
        final var components = MiniaturizationRecipeComponents.CODEC
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, testHelper::fail);

        return new ComponentTestHelper(this.testHelper, components);
    }
}