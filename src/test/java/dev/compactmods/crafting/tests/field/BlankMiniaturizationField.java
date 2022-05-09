package dev.compactmods.crafting.tests.field;

import java.util.Optional;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.MiniaturizationField;
import dev.compactmods.crafting.api.field.FieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

public class BlankMiniaturizationField implements MiniaturizationField {
    @Override
    public AABB getBounds() {
        return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    }

    @Override
    public FieldSize getFieldSize() {
        return FieldSize.INACTIVE;
    }

    @Override
    public BlockPos getCenter() {
        return BlockPos.ZERO;
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
    public EnumCraftingState getCraftingState() {
        return EnumCraftingState.NOT_MATCHED;
    }

    @Override
    public void fieldContentsChanged() {

    }

    @Override
    public void registerListener(LazyOptional<IFieldListener> listener) {

    }

    @Override
    public void setProgress(int progress) {

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
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {

    }
}
