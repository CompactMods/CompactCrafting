package dev.compactmods.crafting.tests.api.field;

import java.util.Optional;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IFieldListener;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public class BlankMiniaturizationField implements IMiniaturizationField {
    @Override
    public AxisAlignedBB getBounds() {
        return AxisAlignedBB.ofSize(0, 0, 0);
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
    public void setLevel(World level) {

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
}
