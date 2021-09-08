package dev.compactmods.crafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public interface IMiniaturizationField {

    AxisAlignedBB getBounds();

    FieldProjectionSize getFieldSize();

    BlockPos getCenter();

    void setCenter(BlockPos center);

    void setSize(FieldProjectionSize size);

    int getProgress();

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    Optional<IMiniaturizationRecipe> getCurrentRecipe();

    void clearRecipe();

    void completeCraft();

    EnumCraftingState getCraftingState();

    void doRecipeScan();

    void setCraftingState(EnumCraftingState state);

    default void tick() {
    }

    boolean isLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    void setLevel(World level);

    void registerListener(LazyOptional<IFieldListener> listener);

    CompoundNBT serverData();

    void loadServerData(CompoundNBT nbt);

    default CompoundNBT clientData() {
        CompoundNBT data = new CompoundNBT();
        data.putLong("center", getCenter().asLong());
        data.putString("size", getFieldSize().name());
        data.putString("state", getCraftingState().name());

        Optional<IMiniaturizationRecipe> currentRecipe = getCurrentRecipe();
        currentRecipe.ifPresent(r -> {
            CompoundNBT recipe = new CompoundNBT();
            recipe.putString("id", r.getRecipeIdentifier().toString());
            recipe.putInt("progress", getProgress());

            data.put("recipe", recipe);
        });

        return data;
    }

    default void loadClientData(CompoundNBT nbt) {
        this.setCenter(BlockPos.of(nbt.getLong("center")));
        this.setSize(FieldProjectionSize.valueOf(nbt.getString("size")));
        this.setCraftingState(EnumCraftingState.valueOf(nbt.getString("state")));

        if (nbt.contains("recipe")) {
            CompoundNBT recipe = nbt.getCompound("recipe");
            this.setRecipe(new ResourceLocation(recipe.getString("id")));
            this.setProgress(recipe.getInt("progress"));
        }
    }

    void setProgress(int progress);

    void setRecipe(ResourceLocation id);
}
