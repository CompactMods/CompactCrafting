package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface IMutableMiniaturizationField {
    void setRecipe(RecipeHolder<MiniaturizationRecipe> recipe);

    void clearRecipe();

    void setCraftingState(EnumCraftingState state);

    void enable();

    void disable();

    void spawnParticlesAtProjectors(ParticleOptions options);
}
