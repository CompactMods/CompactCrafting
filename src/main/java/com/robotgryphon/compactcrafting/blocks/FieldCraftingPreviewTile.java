package com.robotgryphon.compactcrafting.blocks;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipeManager;
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
    private FieldProjectorTile masterProjector;
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

    public void setMasterProjector(FieldProjectorTile master) {
        this.masterProjector = master;
        this.recipe = master.getCurrentRecipe().get();
        this.markDirty();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos).grow(6.0f);
    }

    @Override
    public void tick() {
        this.craftingProgress++;
        if(world.isRemote) {
            return;
        }

        if(this.craftingProgress >= 200) {
            if(masterProjector != null) {
                masterProjector.updateCraftingState(EnumCraftingState.DONE);

                BlockPos fieldCenter = masterProjector.getField().get().getCenterPosition();

                getRecipe().ifPresent(recipe -> {
                    for (ItemStack is : recipe.getOutputs()) {
                        ItemEntity itemEntity = new ItemEntity(world, fieldCenter.getX() + 0.5f, fieldCenter.getY() + 0.5f, fieldCenter.getZ() + 0.5f, is);
                        world.addEntity(itemEntity);
                    }
                });
            }

            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        craftingProgress = compound.getInt("progress");

        if(compound.contains("recipe")) {
            ResourceLocation recipeId = new ResourceLocation(compound.getString("recipe"));
            this.recipe = MiniaturizationRecipeManager.get(recipeId).orElse(null);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(recipe != null) {
            compound.putString("recipe", recipe.getRegistryName().toString());
        }

        compound.putInt("progress", craftingProgress);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        read(getBlockState(), packet.getNbtCompound());
    }
}
