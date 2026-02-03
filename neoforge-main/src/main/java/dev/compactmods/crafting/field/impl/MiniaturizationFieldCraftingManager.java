package dev.compactmods.crafting.field.impl;

import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.field.FieldHelper;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.util.CraftingHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class MiniaturizationFieldCraftingManager {
    private final MiniaturizationField field;
    RecipeHolder<MiniaturizationRecipe> currentRecipe = null;
    private int craftingProgress = 0;

    // Crafting State
    private StructureTemplate matchedBlocks;
    EnumCraftingState craftingState;

    /// If non-zero, tells how many ticks must pass until the crafting system
    /// performs a new scan of the field for a valid recipe.
    private long rescanTicksRemaining;

    MiniaturizationFieldCraftingManager(MiniaturizationField field) {
        this.field = field;
        this.craftingProgress = 0;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
        this.rescanTicksRemaining = 0;
    }

    public int getProgress() {
        if (craftingState != EnumCraftingState.CRAFTING)
            return 0;

        return craftingProgress;
    }

    public void setRecipe(RecipeHolder<MiniaturizationRecipe> recipe) {
        this.currentRecipe = recipe;
        this.craftingProgress = 0;

        if (craftingState == EnumCraftingState.NOT_MATCHED)
            this.craftingState = EnumCraftingState.MATCHED;
    }

    public void clearRecipe() {
        this.currentRecipe = null;
        this.craftingProgress = 0;
        this.craftingState = EnumCraftingState.NOT_MATCHED;
        this.rescanTicksRemaining = 0;
    }

    public EnumCraftingState getCraftingState() {
        return craftingState;
    }

    public boolean scheduleScan() {
        if (this.craftingState == EnumCraftingState.NOT_MATCHED) {
            this.rescanTicksRemaining = 60;
            return true;
        }

        this.rescanTicksRemaining = 0;
        return false;
    }

    public void scan() {
        this.rescanTicksRemaining = 0;

        RecipeScanner.doRecipeScan(field.fieldAccess()).ifPresentOrElse(recipeScanResult -> {
            this.matchedBlocks = recipeScanResult.matchedBlocks();
            this.currentRecipe = recipeScanResult.recipe();
            this.craftingState = EnumCraftingState.MATCHED;
            FieldHelper.spawnParticlesAtProjectors(field, field.level(), MiniaturizationFieldParticles.RECIPE_MATCHED_PARTICLE_OPTS);
        }, () -> {
            this.matchedBlocks = null;
            clearRecipe();
        });
    }

    public void tick() {
        switch (this.craftingState) {
            case NOT_MATCHED:
                if (rescanTicksRemaining > 0 && --rescanTicksRemaining == 0)
                    scan();

                break;

            case MATCHED:
                final var catalysts = RecipeScanner.getCatalystsInField(this.field.fieldAccess(), this.currentRecipe.value());
                if (!catalysts.isEmpty()) {
                    // Only remove items and clear the projectors on servers
                    CraftingHelper.consumeCatalystItem(catalysts.getFirst(), 1);

                    // We know the "recipe" in the projectors is an exact match already, so wipe the projectors
                    this.field.fieldAccess().clearBlocks();

                    this.craftingState = EnumCraftingState.CRAFTING;
                }

                break;

            case CRAFTING:
                tickCrafting();
                break;
        }
    }

    private void tickCrafting() {
        if (this.currentRecipe == null)
            return;

        craftingProgress++;
        final var level = field.level();
        if (craftingProgress >= currentRecipe.value().getCraftingTime()) {
            final var center = field.location().center();
            for (ItemStack is : currentRecipe.value().getOutputs()) {
                level.addFreshEntity(new ItemEntity(level, center.x(), center.y(), center.z(), is));
                level.playLocalSound(center.x(), center.y(), center.z(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS,
                        1.0f, 1.0f, false);
            }

            FieldHelper.spawnParticlesAtProjectors(field, level,
                    MiniaturizationFieldParticles.RECIPE_FINISHED_PARTICLE_OPTS);

            IMiniaturizationRecipe completed = this.currentRecipe.value();
            clearRecipe();
        }
    }
}
