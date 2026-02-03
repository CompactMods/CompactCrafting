package dev.compactmods.crafting.field.impl;

import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.RecipeHelper;
import dev.compactmods.crafting.util.CraftingHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class MiniaturizationFieldCraftingManager {

    private final MiniaturizationField field;


    @Nullable
    private MatchedMiniaturizationRecipe matchedRecipe;
    private MiniaturizationRecipe recipe;

    private int craftingProgress;

    /// If non-zero, tells how many ticks must pass until the crafting system
    /// performs a new scan of the field for a valid recipe.
    private long rescanTicksRemaining;

    MiniaturizationFieldCraftingManager(MiniaturizationField field) {
        this.field = field;
        this.craftingProgress = 0;
        this.rescanTicksRemaining = 0;
    }

    public void loadState(CraftingState state) {
        Objects.requireNonNull(field);
        this.craftingProgress = state.craftingProgress();
        this.rescanTicksRemaining = 0;

        state.matchedRecipe().ifPresent(matched -> {
            this.matchedRecipe = matched;
            this.recipe = RecipeHelper.getRecipe(this.field.level(), matched.recipe())
                    .map(RecipeHolder::value)
                    .orElse(null);
        });
    }

    public CraftingState state() {
        return new CraftingState(Optional.ofNullable(matchedRecipe), this.craftingProgress);
    }

    public int progress() {
        return craftingProgress;
    }

    public void clearRecipe() {
        this.matchedRecipe = null;
        this.craftingProgress = 0;
        this.rescanTicksRemaining = 0;
    }

    public boolean scheduleScan() {
        if (this.matchedRecipe == null) {
            this.rescanTicksRemaining = 60;
            return true;
        }

        this.rescanTicksRemaining = 0;
        return false;
    }

    private void scan() {
        this.rescanTicksRemaining = 0;

        RecipeScanner.doRecipeScan(field.fieldAccess()).ifPresentOrElse(scanResult -> {
            this.matchedRecipe = new MatchedMiniaturizationRecipe(scanResult.recipe().id().identifier(), scanResult.matchedBlocks());
            this.recipe = scanResult.recipe().value();
            this.craftingProgress = 0;

            FieldHelper.spawnParticlesAtProjectors(field, field.level(), MiniaturizationFieldParticles.RECIPE_MATCHED_PARTICLE_OPTS);
        }, this::clearRecipe);
    }

    public void tick() {
        if (matchedRecipe != null) {
            // If we haven't started crafting yet, look for catalysts
            if(craftingProgress == 0) {
                final var catalysts = RecipeScanner.getCatalystsInField(this.field.fieldAccess(), this.recipe);

                // Found a catalyst - try to consume and kick off crafting process
                if (!catalysts.isEmpty() && CraftingHelper.consumeCatalystItem(catalysts.getFirst(), 1)) {
                    this.field.fieldAccess().clearBlocks();
                    this.craftingProgress = 1;
                }

                return;
            }

            tickCrafting();
            return;
        }

        if (rescanTicksRemaining > 0 && --rescanTicksRemaining == 0)
            scan();
    }

    private void tickCrafting() {
        if (this.matchedRecipe == null)
            return;

        final var level = field.level();

        if (++craftingProgress >= recipe.getCraftingTime()) {
            final var center = field.location().center();

            for (ItemStack is : recipe.getOutputs()) {
                level.addFreshEntity(new ItemEntity(level, center.x(), center.y(), center.z(), is));
                level.playLocalSound(center.x(), center.y(), center.z(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS,
                        1.0f, 1.0f, false);
            }

            FieldHelper.spawnParticlesAtProjectors(field, level,
                    MiniaturizationFieldParticles.RECIPE_FINISHED_PARTICLE_OPTS);

            clearRecipe();
        }
    }
}
