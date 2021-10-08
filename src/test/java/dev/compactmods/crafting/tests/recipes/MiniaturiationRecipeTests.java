package dev.compactmods.crafting.tests.recipes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeBlocks;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.blocks.RecipeBlocks;
import dev.compactmods.crafting.recipes.setup.FakeInventory;
import dev.compactmods.crafting.server.ServerConfig;
import dev.compactmods.crafting.tests.recipes.util.RecipeTestUtil;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@IntegrationTestClass("recipes")
public class MiniaturiationRecipeTests {

    private static RecipeManager RECIPES;

    @Tag("minecraft")
    @org.junit.jupiter.api.BeforeAll
    static void BeforeAllTests() {
        ServerConfig.RECIPE_REGISTRATION.set(true);
        ServerConfig.RECIPE_MATCHING.set(true);
        ServerConfig.FIELD_BLOCK_CHANGES.set(true);

        RECIPES = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
    }

    @Nullable
    private MiniaturizationRecipe getRecipe(String name) {
        return (MiniaturizationRecipe) RECIPES.byKey(new ResourceLocation("compactcrafting", name)).orElse(null);
    }

    @Test
    @Tag("minecraft")
    void CanCreate() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
    }

    @Test
    @Tag("minecraft")
    void CanSetId() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            recipe.setId(new ResourceLocation(CompactCrafting.MOD_ID, "test"));
        });
    }


    @Test
    @Tag("minecraft")
    void IsSpecialRecipe() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);
        Assertions.assertTrue(recipe.isSpecial());
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void FakesFakeInventories(IntegrationTestHelper helper) {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = recipe.matches(new FakeInventory(), helper.getWorld());
            Assertions.assertTrue(matched);
        });
    }

    @Test
    @Tag("minecraft")
    void FakesAssemble() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            ItemStack result = recipe.assemble(new FakeInventory());
            Assertions.assertTrue(result.isEmpty());
        });
    }

    @Test
    @Tag("minecraft")
    void FakesCanCraftDimensions() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            boolean canCraft = recipe.canCraftInDimensions(0, 0);
            Assertions.assertTrue(canCraft);
        });
    }

    @Test
    @Tag("minecraft")
    void FakesResultItem() {
        MiniaturizationRecipe recipe = new MiniaturizationRecipe();
        Assertions.assertNotNull(recipe);

        Assertions.assertDoesNotThrow(() -> {
            final ItemStack result = recipe.getResultItem();
            Assertions.assertTrue(result.isEmpty());
        });
    }

    @Test
    @Tag("minecraft")
    void RecipeSuppliesBasicMinecraftRegistrationInfo() {
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        Assertions.assertNotNull(enderCrystal);

        final IRecipeSerializer<?> serializer = Assertions.assertDoesNotThrow(enderCrystal::getSerializer);
        Assertions.assertNotNull(serializer);

        final IRecipeType<?> type = Assertions.assertDoesNotThrow(enderCrystal::getType);
        Assertions.assertNotNull(type);
    }



    @Test
    @Tag("minecraft")
    void RecipeReturnsEmptyIfLayerNotRegistered() {
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        Assertions.assertNotNull(enderCrystal);

        final Optional<IRecipeLayer> layer = Assertions.assertDoesNotThrow(() -> enderCrystal.getLayer(999));
        Assertions.assertFalse(layer.isPresent());
    }

    @Test
    @Tag("minecraft")
    void FitsInCorrectFieldSizes() {
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        Assertions.assertNotNull(enderCrystal);

        MiniaturizationFieldSize[] badSizes = new MiniaturizationFieldSize[]{
                MiniaturizationFieldSize.INACTIVE, MiniaturizationFieldSize.SMALL
        };

        MiniaturizationFieldSize[] goodSizes = new MiniaturizationFieldSize[]{
                MiniaturizationFieldSize.MEDIUM, MiniaturizationFieldSize.LARGE, MiniaturizationFieldSize.ABSURD
        };

        for (MiniaturizationFieldSize bs : badSizes)
            Assertions.assertFalse(enderCrystal.fitsInFieldSize(bs), "Fit in bad field size: " + bs);

        for (MiniaturizationFieldSize gs : goodSizes)
            Assertions.assertTrue(enderCrystal.fitsInFieldSize(gs), "Did not fit in field size: " + gs);
    }

    @Test
    @Tag("minecraft")
    void CanGetComponentTotals() {
        final MiniaturizationRecipe recipe = getRecipe("ender_crystal");
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

    @Test
    @Tag("minecraft")
    void UnregisteredBlockReturnsZeroCount() {
        final MiniaturizationRecipe recipe = getRecipe("ender_crystal");
        Assertions.assertNotNull(recipe);

        final int required = Assertions.assertDoesNotThrow(() -> recipe.getComponentRequiredCount("?"));
        Assertions.assertEquals(0, required);
    }

    @Test
    @Tag("minecraft")
    void HasCraftingTime() {
        final MiniaturizationRecipe recipe = getRecipe("ender_crystal");
        Assertions.assertNotNull(recipe);

        final int required = Assertions.assertDoesNotThrow(recipe::getCraftingTime);
        Assertions.assertNotEquals(0, required);
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void MatchesExactStructure(IntegrationTestHelper helper) {
        final BlockPos zero = helper.relativePos(BlockPos.ZERO).get();
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        final IRecipeBlocks blocks = RecipeBlocks
                .create(helper.getWorld(), enderCrystal.getComponents(), enderCrystal.getDimensions().move(zero))
                .normalize();

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = enderCrystal.matches(blocks);
            Assertions.assertTrue(matched);
        });
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void RecipeFailsIfUnidentifiedBlock(IntegrationTestHelper helper) {
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        Assertions.assertNotNull(enderCrystal);

        // Force an unknown component in the exact center
        helper.setBlockState(new BlockPos(2, 2, 2), Blocks.GOLD_BLOCK.defaultBlockState());

        final IRecipeBlocks blocks = RecipeBlocks
                .create(helper.getWorld(), enderCrystal.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        Assertions.assertDoesNotThrow(() -> {
            boolean matched = enderCrystal.matches(blocks);
            Assertions.assertFalse(matched, "Recipe did not fail the matching process.");
        });
    }

    @Test
    @Tag("minecraft")
    void CanStreamLayerInfo() {
        final MiniaturizationRecipe enderCrystal = getRecipe("ender_crystal");
        final Stream<IRecipeLayer> strem = Assertions.assertDoesNotThrow(enderCrystal::getLayers);

        Assertions.assertNotNull(strem);

        final Set<IRecipeLayer> layers = strem.collect(Collectors.toSet());
        Assertions.assertEquals(5, layers.size());
    }

    @Tag("minecraft")
    @IntegrationTest("ender_crystal")
    void RecipeFailsIfDifferentDimensions(IntegrationTestHelper helper) {
        final MiniaturizationRecipe recipe = getRecipe("compact_walls");
        Assertions.assertNotNull(recipe);

        final IRecipeBlocks blocks = RecipeBlocks
                .create(helper.getWorld(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        final boolean matched = Assertions.assertDoesNotThrow(() -> recipe.matches(blocks));
        Assertions.assertFalse(matched, "Recipe matched even though dimensions are different.");
    }

    @Tag("minecraft")
    @IntegrationTest("empty_medium")
    void RecipeFailsIfNoRotationsMatched(IntegrationTestHelper helper) {
        final MiniaturizationRecipe recipe = getRecipe("ender_crystal");
        Assertions.assertNotNull(recipe);

        // Set up the 8 corners to be glass, so block creation below matches field boundaries
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockSpaceUtil.getCornersOfBounds(MiniaturizationFieldSize.MEDIUM).forEach(p -> helper.setBlockState(p, glass));

        final IRecipeBlocks blocks = RecipeBlocks
                .create(helper.getWorld(), recipe.getComponents(), RecipeTestUtil.getFieldBounds(MiniaturizationFieldSize.MEDIUM, helper))
                .normalize();

        final boolean matched = Assertions.assertDoesNotThrow(() -> recipe.matches(blocks));
        Assertions.assertFalse(matched, "Recipe matched even though blocks are different. (Spatial dimensions equal.)");
    }
}
