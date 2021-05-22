package com.robotgryphon.compactcrafting.field.tile;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.projector.tile.MainFieldProjectorTile;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
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
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class FieldCraftingPreviewTile extends TileEntity implements ITickableTileEntity {
    private MainFieldProjectorTile masterProjector;
    private int craftingProgress = 0;
    private MiniaturizationRecipe recipe;

    public FieldCraftingPreviewTile() {
        super(Registration.FIELD_CRAFTING_PREVIEW_TILE.get());
    }

    public int getProgress() {
        return this.craftingProgress;
    }

    public Optional<MiniaturizationRecipe> getRecipe() {
        return Optional.ofNullable(recipe);
    }

    public void setMasterProjector(MainFieldProjectorTile master) {
        this.masterProjector = master;
        this.recipe = master.getCurrentRecipe().get();
        this.setChanged();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition).inflate(6.0f);
    }

    @Override
    public void tick() {
        this.craftingProgress++;
        if(level.isClientSide) {
            return;
        }

        if(this.craftingProgress >= 200) {
            if(masterProjector != null) {
                masterProjector.updateCraftingState(EnumCraftingState.DONE);

                BlockPos fieldCenter = masterProjector.getField().get().getCenterPosition();

                getRecipe().ifPresent(recipe -> {
                    for (ItemStack is : recipe.getOutputs()) {
                        ItemEntity itemEntity = new ItemEntity(level, fieldCenter.getX() + 0.5f, fieldCenter.getY() + 0.5f, fieldCenter.getZ() + 0.5f, is);
                        level.addFreshEntity(itemEntity);
                    }
                });
            }

            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        craftingProgress = compound.getInt("progress");

        if(compound.contains("recipe")) {
            ResourceLocation recipeId = new ResourceLocation(compound.getString("recipe"));
            level.getRecipeManager()
                    .byKey(recipeId)
                    .ifPresent(recipe -> this.recipe = (MiniaturizationRecipe) recipe);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if(recipe != null) {
            compound.putString("recipe", recipe.getId().toString());
        }

        compound.putInt("progress", craftingProgress);
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
}
