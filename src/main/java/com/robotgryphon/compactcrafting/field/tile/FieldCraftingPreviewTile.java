package com.robotgryphon.compactcrafting.field.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.compactcrafting.api.field.IActiveWorldFields;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;

@Deprecated
public class FieldCraftingPreviewTile extends TileEntity implements ITickableTileEntity {

    @Nonnull
    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();
    private int craftingProgress = 0;

    @Nullable
    private IMiniaturizationRecipe recipe;
    private ResourceLocation recipeId;
    private LazyOptional<IActiveWorldFields> worldFields = LazyOptional.empty();

    public FieldCraftingPreviewTile() {
        super(Registration.FIELD_CRAFTING_PREVIEW_TILE.get());
    }

    public int getProgress() {
        return this.craftingProgress;
    }

    @Nullable
    public IMiniaturizationRecipe getRecipe() {
        return recipe;
    }

    public void setField(IMiniaturizationField field) {

    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition).inflate(6.0f);
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (recipe == null || recipeId == null) {
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
            return;
        }

        for (ItemStack is : recipe.getOutputs()) {
            ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f, is);
            level.addFreshEntity(itemEntity);
        }

        level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        craftingProgress = compound.getInt("progress");
        recipeId = new ResourceLocation(compound.getString("recipe"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            worldFields = level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS);
            recipe = (IMiniaturizationRecipe) level.getRecipeManager().byKey(recipeId).orElse(null);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("progress", craftingProgress);
        if (recipeId != null)
            compound.putString("recipe", recipeId.toString());

        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        load(getBlockState(), packet.getTag());
    }

    public void setRecipe(IMiniaturizationRecipe recipe) {
        if (recipe != null) {
            this.recipe = recipe;
            this.recipeId = recipe.getId();
            this.craftingProgress = 0;
        }

        setChanged();
    }
}
