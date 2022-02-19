package dev.compactmods.crafting.tests.recipes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// @MCTestClass
public class MiniaturizationRecipes {

    @Nullable
    private static MiniaturizationRecipe getRecipe(GameTestHelper testHelper, String name) {
        return (MiniaturizationRecipe) testHelper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation("compactcrafting", name))
                .orElse(null);
    }

    @Test
    void CanCreateRecipe() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
    }

    @Test
    void CanSetId() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            recipe.setId(new ResourceLocation(CompactCrafting.MOD_ID, "test"));
        });
    }


    @Test
    void IsSpecialRecipe() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
        Assertions.assertTrue(recipe.isSpecial());
    }


    // @StructureFile("ender_crystal")
    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID)
    public static void FakesFakeInventories(final GameTestHelper test) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();

        try {
            boolean matched = recipe.matches(new FakeInventory(), test.getLevel());
            if(!matched)
                test.fail("Expected fake inventory to always match.");
        }

        catch (Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void FakesAssemble(final GameTestHelper test) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();

        try {
            ItemStack result = recipe.assemble(new FakeInventory());
            if(!result.isEmpty())
                test.fail("Expected an empty result");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @Test
    void FakesCanCraftDimensions() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean canCraft = recipe.canCraftInDimensions(0, 0);
            Assertions.assertTrue(canCraft);
        });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void FakesResultItem(final GameTestHelper test) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();

        try{
            final ItemStack result = recipe.getResultItem();
            if(!result.isEmpty())
                test.fail("Expected recipe result to be empty.");
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }

        test.succeed();
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void RecipeSuppliesBasicMinecraftRegistrationInfo(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        if(enderCrystal == null)
            return;

        final var serializer = enderCrystal.getSerializer();
        if(serializer == null)
            test.fail("Did not get a recipe serializer from the recipe class.");

        final var type = enderCrystal.getType();
        if(type == null)
            test.fail("Did not get a recipe type from the recipe class.");

        test.succeed();
    }


    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void RecipeReturnsEmptyIfLayerNotRegistered(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Objects.requireNonNull(enderCrystal);

        final Optional<IRecipeLayer> layer = enderCrystal.getLayer(999);
        if(layer.isPresent())
            test.fail("Layer should not have been present.");

        test.succeed();
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
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
            if(enderCrystal.fitsInFieldSize(bs))
                test.fail("Fit in bad field size: " + bs);

        for (MiniaturizationFieldSize gs : goodSizes)
            if(!enderCrystal.fitsInFieldSize(gs))
                test.fail("Did not fit in field size: " + gs);

        test.succeed();
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanGetComponentTotals(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Assertions.assertNotNull(recipe);

        final Map<String, Integer> totals = Assertions.assertDoesNotThrow(recipe::getComponentTotals);
        Assertions.assertNotNull(totals);
        Assertions.assertEquals(2, totals.size()); // expect 2 (G, O)

        for (String key : new String[]{"G", "O"})
            Assertions.assertTrue(totals.containsKey(key), "Totals did not contain key: " + key);

        final Map<String, Integer> maybeCached = Assertions.assertDoesNotThrow(recipe::getComponentTotals);
        Assertions.assertSame(totals, maybeCached);

        final Integer totalObsidian = Assertions.assertDoesNotThrow(() -> recipe.getComponentRequiredCount("O"));
        Assertions.assertEquals(1, totalObsidian);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void UnregisteredBlockReturnsZeroCount(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Assertions.assertNotNull(recipe);

        final int required = Assertions.assertDoesNotThrow(() -> recipe.getComponentRequiredCount("?"));
        Assertions.assertEquals(0, required);
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void HasCraftingTime(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Assertions.assertNotNull(recipe);

        final int required = Assertions.assertDoesNotThrow(recipe::getCraftingTime);
        Assertions.assertNotEquals(0, required);
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID)
    public static void MatchesExactStructure(final GameTestHelper test) {
        final BlockPos zero = test.relativePos(BlockPos.ZERO);
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), enderCrystal.getDimensions().move(zero))
                .normalize();

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = enderCrystal.matches(blocks);
            Assertions.assertTrue(matched);
        });
    }


    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID)
    public static void RecipeFailsIfUnidentifiedBlock(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        Assertions.assertNotNull(enderCrystal);

        // Force an unknown component in the exact center
        test.setBlock(new BlockPos(2, 2, 2), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = enderCrystal.matches(blocks);
            Assertions.assertFalse(matched, "Recipe did not fail the matching process.");
        });
    }

    @GameTest(template = "empty_medium", templateNamespace = CompactCrafting.MOD_ID, prefixTemplateWithClassname = false)
    public static void CanStreamLayerInfo(final GameTestHelper test) {
        final MiniaturizationRecipe enderCrystal = getRecipe(test, "ender_crystal");
        final Stream<IRecipeLayer> strem = Assertions.assertDoesNotThrow(enderCrystal::getLayers);

        Assertions.assertNotNull(strem);

        final Set<IRecipeLayer> layers = strem.collect(Collectors.toSet());
        Assertions.assertEquals(5, layers.size());
    }

    @GameTest(template = "recipes/ender_crystal", templateNamespace = CompactCrafting.MOD_ID)
    public static void RecipeFailsIfDifferentDimensions(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "compact_walls");
        Assertions.assertNotNull(recipe);

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = Assertions.assertDoesNotThrow(() -> recipe.matches(blocks));
        Assertions.assertFalse(matched, "Recipe matched even though dimensions are different.");
    }


    @GameTest(template = "recipes/empty_medium", templateNamespace = CompactCrafting.MOD_ID)
    public static void RecipeFailsIfNoRotationsMatched(final GameTestHelper test) {
        final MiniaturizationRecipe recipe = getRecipe(test, "ender_crystal");
        Assertions.assertNotNull(recipe);

        // Set up the 8 corners to be glass, so block creation below matches field boundaries
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockSpaceUtil.getCornersOfBounds(MiniaturizationFieldSize.MEDIUM).forEach(p -> test.setBlock(p, glass));

        final IRecipeBlocks blocks = RecipeBlocks
                .create(test.getLevel(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, test))
                .normalize();

        final boolean matched = Assertions.assertDoesNotThrow(() -> recipe.matches(blocks));
        Assertions.assertFalse(matched, "Recipe matched even though blocks are different. (Spatial dimensions equal.)");
    }
}
