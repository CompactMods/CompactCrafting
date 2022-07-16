package dev.compactmods.crafting.tests.recipes;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class MiniaturizationRecipeTests {

    @Nullable
    private static MiniaturizationRecipe getRecipe(GameTestHelper testHelper, String name) {
        return (MiniaturizationRecipe) testHelper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .orElse(null);
    }

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCreateRecipe(final GameTestHelper test) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        GameTestAssertions.assertNotNull(recipe);
        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void RecipeReturnsEmptyIfLayerNotRegistered(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        final Optional<IRecipeLayer> layer = enderCrystal.getLayer(999);
        if (layer.isPresent())
            test.fail("Layer should not have been present.");

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void FitsInCorrectFieldSizes(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        MiniaturizationFieldSize[] badSizes = new MiniaturizationFieldSize[]{
                MiniaturizationFieldSize.INACTIVE, MiniaturizationFieldSize.SMALL
        };

        MiniaturizationFieldSize[] goodSizes = new MiniaturizationFieldSize[]{
                MiniaturizationFieldSize.MEDIUM, MiniaturizationFieldSize.LARGE, MiniaturizationFieldSize.ABSURD
        };

        // TODO: GameTestGenerator?
        for (MiniaturizationFieldSize bs : badSizes)
            if (enderCrystal.fitsInFieldSize(bs))
                test.fail("Fit in bad field size: " + bs);

        for (MiniaturizationFieldSize gs : goodSizes)
            if (!enderCrystal.fitsInFieldSize(gs))
                test.fail("Did not fit in field size: " + gs);

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void CanGetComponentTotals(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(recipe);

        final Map<String, Integer> totals = recipe.getComponentTotals();
        if (totals == null) {
            test.fail("Returned totals should not be null.");
            return;
        }

        if (2 != totals.size()) {
            // expect 2 (G, O)
            test.fail("Expected exactly two components found (G,O). Got (" + String.join(",", totals.keySet()) + ")");
        }

        for (String key : new String[]{"G", "O"}) {
            if (!totals.containsKey(key))
                test.fail("Totals did not contain key: " + key);
        }

        final Map<String, Integer> maybeCached = recipe.getComponentTotals();
        if(totals != maybeCached) {
            test.fail("Component totals should be cached after first call.");
        }

        final int totalObsidian = recipe.getComponentRequiredCount("O");
        if(totalObsidian != 1)
            test.fail("Expected exactly 1 obsidian block to be required.");

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void UnregisteredBlockReturnsZeroCount(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(recipe);

        final int required = recipe.getComponentRequiredCount("?");
        if(required != 0)
            test.fail("Unknown component returned a non-zero count.");

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void HasCraftingTime(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(recipe);

        final int required = recipe.getCraftingTime();
        if(required == 0)
            test.fail("Expected recipe to have a default, non-zero crafting time.");

        test.succeed();
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void MatchesExactStructure(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        try {
            boolean matched = enderCrystal.matches(blocks);
            if(!matched) {
                test.fail("Recipe should have matched.");
            }

            test.succeed();
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void RecipeFailsIfUnidentifiedBlock(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        // Force an unknown component in the exact center
        test.setBlock(new BlockPos(2, 2, 2), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        try {
            boolean matched = enderCrystal.matches(blocks);
            if(matched)
                test.fail("Recipe did not fail the matching process.");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium")
    public static void CanStreamLayerInfo(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        final Stream<IRecipeLayer> layers1 = enderCrystal.getLayers();

        if(layers1 == null) {
            test.fail("Recipe did not create a stream of layers correctly.");
            return;
        }

        final Set<IRecipeLayer> layers = layers1.collect(Collectors.toSet());
        if(5 != layers.size()) {
            test.fail("Expected 5 layers; got " + layers.size());
        }

        test.succeed();
    }

    @GameTest(template = "recipes/ender_crystal")
    public static void RecipeFailsIfDifferentDimensions(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "compact_walls");
        Objects.requireNonNull(recipe);

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = recipe.matches(blocks);
        if(matched)
            test.fail("Recipe matched even though dimensions are different.");

        test.succeed();
    }

    @GameTest(template = "recipes/empty_medium")
    public static void RecipeFailsIfNoRotationsMatched(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(recipe);

        // Set up the 8 corners to be glass, so block creation below matches field boundaries
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockSpaceUtil.getCornersOfBounds(MiniaturizationFieldSize.MEDIUM).forEach(p -> test.setBlock(p, glass));

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = recipe.matches(blocks);
        if(matched)
            test.fail("Recipe matched even though blocks are different. (Spatial dimensions equal.)");

        test.succeed();
    }
}
