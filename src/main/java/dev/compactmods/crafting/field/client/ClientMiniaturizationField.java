package dev.compactmods.crafting.field.client;

import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.*;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class ClientMiniaturizationField implements ClientField, MiniaturizationField {

    private final Level level;
    private final FieldSize size;
    private final BlockPos center;
    private EnumCraftingState craftingState;

    @Nullable
    private MiniaturizationRecipe currentRecipe = null;
    private ResourceLocation recipeId = null;

    public ClientMiniaturizationField(Level level, FieldSize size, BlockPos center, CompoundTag fieldData) {
        this.size = size;
        this.center = center;
        this.level = level;

        this.craftingState = EnumCraftingState.valueOf(fieldData.getString("state"));
        if (fieldData.contains("recipe")) {
            CompoundTag recipe = fieldData.getCompound("recipe");
            setProgress(recipe.getInt("progress"));
            setRecipe(new ResourceLocation(recipe.getString("id")));
        }
    }

    private void setRecipe(ResourceLocation id) {
        this.recipeId = id;
        getRecipeFromId();
    }

    private void getRecipeFromId() {
        // Load recipe information from temporary id variable
        if (level != null && this.recipeId != null) {
            final Optional<? extends Recipe<?>> r = level.getRecipeManager().byKey(recipeId);
            if (r.isEmpty()) {
                clearRecipe();
                return;
            }

            r.ifPresent(rec -> {
                this.currentRecipe = (MiniaturizationRecipe) rec;
                if (craftingState == EnumCraftingState.NOT_MATCHED)
                    this.craftingState = EnumCraftingState.MATCHED;
            });
        } else {
            clearRecipe();
        }
    }

    private void clearRecipe() {
        this.recipeId = null;
        this.currentRecipe = null;
    }

    @Override
    public ProjectorRenderStyle renderStyle() {
        return null;
    }

    @Override
    public void setRenderStyle(ProjectorRenderStyle style) {

    }

    @Override
    public AABB getBounds() {
        return size.getBoundsAtPosition(center);
    }

    @Override
    public FieldSize getFieldSize() {
        return size;
    }

    @Override
    public BlockPos getCenter() {
        return center;
    }

    @Override
    public EnumCraftingState getCraftingState() {
        return null;
    }

    @Override
    public Optional<IMiniaturizationRecipe> getCurrentRecipe() {
        return Optional.ofNullable(currentRecipe);
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public void setProgress(int progress) {

    }

    @Override
    public void fieldContentsChanged() {

    }

    @Override
    public void registerListener(LazyOptional<IFieldListener> listener) {

    }

    @Override
    public LazyOptional<MiniaturizationField> getRef() {
        return null;
    }

    @Override
    public void setRef(LazyOptional<MiniaturizationField> ref) {

    }

    @Override
    public Tag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(Tag nbt) {

    }
}
