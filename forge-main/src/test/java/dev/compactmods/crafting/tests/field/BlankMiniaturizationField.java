package dev.compactmods.crafting.tests.field;

import java.util.Optional;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

public class BlankMiniaturizationField implements IMiniaturizationField {
    @Override
    public AABB getBounds() {
        return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    }

    @Override
    public MiniaturizationFieldSize getFieldSize() {
        return MiniaturizationFieldSize.INACTIVE;
    }

    @Override
    public BlockPos getCenter() {
        return BlockPos.ZERO;
    }

    @Override
    public void setCenter(BlockPos center) {

    }

    @Override
    public void setSize(MiniaturizationFieldSize size) {

    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public Optional<IMiniaturizationRecipe> getCurrentRecipe() {
        return Optional.empty();
    }

    @Override
    public void clearRecipe() {

    }

    @Override
    public EnumCraftingState getCraftingState() {
        return EnumCraftingState.NOT_MATCHED;
    }

    @Override
    public void setCraftingState(EnumCraftingState state) {

    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void fieldContentsChanged() {

    }

    @Override
    public Level level() {
        return null;
    }

    @Override
    public void setLevel(Level level) {

    }

    @Override
    public void registerListener(LazyOptional<IFieldListener> listener) {

    }

    @Override
    public void setProgress(int progress) {

    }

    @Override
    public void setRecipe(ResourceLocation id) {

    }

    @Override
    public LazyOptional<IMiniaturizationField> getRef() {
        return null;
    }

    @Override
    public void setRef(LazyOptional<IMiniaturizationField> ref) {

    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void checkRedstone() {

    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public Tag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {

    }
}
