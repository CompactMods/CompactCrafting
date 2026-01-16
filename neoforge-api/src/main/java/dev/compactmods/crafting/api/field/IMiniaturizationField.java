package dev.compactmods.crafting.api.field;

import java.util.stream.Stream;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.projector.FieldProjectorSet;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface IMiniaturizationField<T extends IMiniaturizationRecipe> {

    default void dispose() {}

    AABB getBounds();

    MiniaturizationFieldSize getFieldSize();

    BlockPos getCenter();

    int getProgress();

    FieldProjectorSet getProjectors();

    EnumCraftingState getCraftingState();

    boolean isAreaLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    default void handleDestabilize() {}

    void checkRedstone();

    boolean enabled();

    RecipeHolder<T> recipeHolder();
    T currentRecipe();
}
