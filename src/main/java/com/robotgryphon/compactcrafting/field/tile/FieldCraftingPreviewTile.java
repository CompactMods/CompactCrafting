package com.robotgryphon.compactcrafting.field.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import dev.compactmods.compactcrafting.api.recipe.IMiniaturizationRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;

public class FieldCraftingPreviewTile extends TileEntity implements ITickableTileEntity {

    // TODO - Treat this like a field listener (read: proxies) and move the progress to the field

    @Nonnull
    private LazyOptional<IMiniaturizationField> field = LazyOptional.empty();
    private int craftingProgress = 0;

    @Nullable
    private IMiniaturizationRecipe recipe;
    private ResourceLocation recipeId;

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
        this.field = LazyOptional.of(() -> field);

        updateRecipe(field.getCurrentRecipe().orElse(null));

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
        if (level == null || level.isClientSide) {
            return;
        }

        if(recipe == null || recipeId == null) {
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
            return;
        }

        // TODO - Clean this up, potential for crash
        // https://discord.com/channels/765363477186740234/851154648140218398/852552351436374066
        if (this.craftingProgress >= recipe.getCraftingTime()) {
            field.ifPresent(IMiniaturizationField::completeCraft);
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
        }
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

        if(!field.isPresent() && level != null) {
            level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    this.field = fields.getLazy(worldPosition);
                });
        }

        field.ifPresent(f -> {
            updateRecipe(f.getCurrentRecipe().orElse(null));
        });
    }

    private void updateRecipe(IMiniaturizationRecipe rec) {
        this.recipe = rec;
        this.recipeId = rec != null ? rec.getId() : null;
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("progress", craftingProgress);
        if(recipeId != null)
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
        this.recipeId = recipe.getId();
        this.recipe = recipe;

        setChanged();
    }
}
