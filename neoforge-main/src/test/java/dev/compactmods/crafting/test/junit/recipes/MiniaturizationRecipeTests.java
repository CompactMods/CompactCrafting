package dev.compactmods.crafting.test.junit.recipes;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.test.junit.recipes.util.JUnitTestHelper;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(EphemeralTestServerProvider.class)
public class MiniaturizationRecipeTests {

    private static @NotNull MiniaturizationRecipe getEnderCrystal() {
        return JUnitTestHelper.getRecipeFromTestRecipes("ender_crystal").orElseThrow();
    }

    @Test
    public void RecipeReturnsEmptyIfLayerNotRegistered(final MinecraftServer server) {
        final MiniaturizationRecipe enderCrystal = getEnderCrystal();

        Objects.requireNonNull(enderCrystal);

        final Optional<IRecipeLayer> layer = enderCrystal.getLayer(999);
        if (layer.isPresent())
            Assertions.fail("Layer should not have been present.");
    }

    @Test
    public void FitsInCorrectFieldSizes(final MinecraftServer server) {
        final MiniaturizationRecipe enderCrystal = getEnderCrystal();
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
                Assertions.fail("Fit in bad field size: " + bs);

        for (MiniaturizationFieldSize gs : goodSizes)
            if (!enderCrystal.fitsInFieldSize(gs))
                Assertions.fail("Did not fit in field size: " + gs);
    }

    @Test
    public void CanGetComponentTotals(final MinecraftServer server) {
        final MiniaturizationRecipe recipe = getEnderCrystal();
        Objects.requireNonNull(recipe);

        final Map<String, Integer> totals = recipe.getComponentTotals();
        if (totals == null) {
            Assertions.fail("Returned totals should not be null.");
            return;
        }

        if (2 != totals.size()) {
            // expect 2 (G, O)
            Assertions.fail("Expected exactly two components found (G,O). Got (" + String.join(",", totals.keySet()) + ")");
        }

        for (String key : new String[]{"G", "O"}) {
            if (!totals.containsKey(key))
                Assertions.fail("Totals did not contain key: " + key);
        }

        final int totalObsidian = recipe.getComponentRequiredCount("O");
        if (totalObsidian != 1)
            Assertions.fail("Expected exactly 1 obsidian block to be required.");
    }

    @Test
    public void UnregisteredBlockReturnsZeroCount(final MinecraftServer server) {
        final MiniaturizationRecipe recipe = getEnderCrystal();
        Objects.requireNonNull(recipe);

        final int required = recipe.getComponentRequiredCount("?");
        if (required != 0)
            Assertions.fail("Unknown component returned a non-zero count.");
    }

    @Test
    public void HasCraftingTime(final MinecraftServer server) {
        final MiniaturizationRecipe recipe = getEnderCrystal();
        Objects.requireNonNull(recipe);

        final int required = recipe.getCraftingTime();
        if (required == 0)
            Assertions.fail("Expected recipe to have a default, non-zero crafting time.");
    }

    @Test
    public void CanStreamLayerInfo(final MinecraftServer server) {
        final MiniaturizationRecipe enderCrystal = getEnderCrystal();
        Objects.requireNonNull(enderCrystal);

        final var layers = enderCrystal.getLayers()
                .collect(Collectors.toSet());

        if (5 != layers.size()) {
            Assertions.fail("Expected 5 layers; got " + layers.size());
        }
    }
}
