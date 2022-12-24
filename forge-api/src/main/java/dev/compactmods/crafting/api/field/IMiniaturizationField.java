package dev.compactmods.crafting.api.field;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public interface IMiniaturizationField extends INBTSerializable<Tag> {

    default void dispose() {}

    AABB getBounds();

    MiniaturizationFieldSize getFieldSize();

    BlockPos getCenter();

    void setCenter(BlockPos center);

    void setSize(MiniaturizationFieldSize size);

    int getProgress();

    default Stream<BlockPos> getProjectorPositions() {
        return Stream.empty();
    }

    Optional<IMiniaturizationRecipe> getCurrentRecipe();

    void clearRecipe();

    EnumCraftingState getCraftingState();

    void setCraftingState(EnumCraftingState state);

    default void tick() {
    }

    boolean isLoaded();

    default void checkLoaded() {
    }

    void fieldContentsChanged();

    Level level();

    void setLevel(Level level);

    void registerListener(LazyOptional<IFieldListener> listener);

    default CompoundTag serverData() {
        return new CompoundTag();
    }

    default CompoundTag clientData() {
        CompoundTag data = new CompoundTag();
        data.putLong("center", getCenter().asLong());
        data.putString("size", getFieldSize().name());
        data.putString("state", getCraftingState().name());

        Optional<IMiniaturizationRecipe> currentRecipe = getCurrentRecipe();
        currentRecipe.ifPresent(r -> {
            CompoundTag recipe = new CompoundTag();
            recipe.putString("id", r.getRecipeIdentifier().toString());
            recipe.putInt("progress", getProgress());

            data.put("recipe", recipe);
        });

        return data;
    }

    default void loadClientData(CompoundTag nbt) {
        this.setCenter(BlockPos.of(nbt.getLong("center")));
        this.setSize(MiniaturizationFieldSize.valueOf(nbt.getString("size")));
        this.setCraftingState(EnumCraftingState.valueOf(nbt.getString("state")));

        if (nbt.contains("recipe")) {
            CompoundTag recipe = nbt.getCompound("recipe");
            this.setRecipe(new ResourceLocation(recipe.getString("id")));
            this.setProgress(recipe.getInt("progress"));
        }
    }

    void setProgress(int progress);

    void setRecipe(ResourceLocation id);

    default void handleDestabilize() {}

    LazyOptional<IMiniaturizationField> getRef();
    void setRef(LazyOptional<IMiniaturizationField> ref);

    void enable();
    void disable();
    void checkRedstone();

    boolean enabled();
}
