package com.robotgryphon.compactcrafting.field.tile;

import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
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
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

public class FieldCraftingPreviewTile extends TileEntity implements ITickableTileEntity {
    @Nonnull
    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();
    private int craftingProgress = 0;
    private MiniaturizationRecipe recipe;

    public FieldCraftingPreviewTile() {
        super(Registration.FIELD_PROXY_TILE.get());
    }

    public int getProgress() {
        return this.craftingProgress;
    }

    public MiniaturizationRecipe getRecipe() {
        return recipe;
    }

    public void setField(IMiniaturizationField field) {
        this.field = LazyOptional.of(() -> field);

        this.recipe = field.getCurrentRecipe().orElse(null);

        // Add invalidation listener so if the field invalidates, this block vanishes and the craft is lost
        this.field.addListener(f -> {
            if(level != null)
                level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        });

        this.setChanged();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition).inflate(6.0f);
    }

    @Override
    public void tick() {
        this.craftingProgress++;
        if (level.isClientSide) {
            return;
        }

        // TODO - Clean this up, potential for crash
        // https://discord.com/channels/765363477186740234/851154648140218398/852552351436374066
        if (this.craftingProgress >= recipe.getTicks()) {
            field.ifPresent(IMiniaturizationField::completeCraft);

            BlockPos center = this.worldPosition;
            if(recipe != null) {
                for (ItemStack is : recipe.getOutputs()) {
                    ItemEntity itemEntity = new ItemEntity(level, center.getX() + 0.5f, center.getY() + 0.5f, center.getZ() + 0.5f, is);
                    level.addFreshEntity(itemEntity);
                }
            }

            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        craftingProgress = compound.getInt("progress");

        if (compound.contains("recipe")) {
            ResourceLocation recipeId = new ResourceLocation(compound.getString("recipe"));
            level.getRecipeManager()
                    .byKey(recipeId)
                    .ifPresent(recipe -> this.recipe = (MiniaturizationRecipe) recipe);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if (recipe != null) {
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
