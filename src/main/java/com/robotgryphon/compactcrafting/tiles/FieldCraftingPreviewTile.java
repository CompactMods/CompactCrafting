package com.robotgryphon.compactcrafting.tiles;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.crafting.EnumCraftingState;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import com.robotgryphon.compactcrafting.network.PlayMiniaturizationSoundPacket;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.RecipeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldCraftingPreviewTile extends TileEntity implements ITickableTileEntity {
    private MainFieldProjectorTile masterProjector;
    private BlockPos masterPos;
    private int craftingProgress = 0;
    private MiniaturizationRecipe recipe;
    private ResourceLocation recipeId;

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
        // Only ever called on the server side
        this.masterProjector = master;
        this.masterPos = master.getBlockPos();
        this.recipe = master.getCurrentRecipe().get();
        double x = this.worldPosition.getX() + 0.5D;
        double y = this.worldPosition.getY() + 0.5D;
        double z = this.worldPosition.getZ() + 0.5D;
        PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(x, y, z, 16, this.level.dimension()));
        NetworkHandler.MAIN_CHANNEL.send(target, new PlayMiniaturizationSoundPacket(this.worldPosition));
        this.setChanged();
    }

    @Override
    public void tick() {
        // Do nothing if the world hasn't loaded yet (we were saved to disk and loaded again?)
        if (this.level == null)
            return;

        // We need this variable on the clientside
        this.craftingProgress++;

        // Be gone, clients!
        if (this.level.isClientSide)
            return;

        // If we are loading the recipe/master projector data
        if (this.recipe == null || this.masterProjector == null) {
            // If the recipeId or masterPos is null, the recipe has been invalidated and we should set to air.
            if (this.recipeId == null || this.masterPos == null) {
                this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                return;
            } else {
                loadData();
                // If one is still null, return
                if (this.recipe == null || this.masterProjector == null)
                    return;
            }
        }

        if (this.craftingProgress >= this.recipe.getTickDuration()) {
            if (!this.masterProjector.isRemoved()) {
                this.masterProjector.updateCraftingState(EnumCraftingState.DONE);
                this.masterProjector.setRecipe(null);
            }

            for (ItemStack is : this.recipe.getOutputs()) {
                ItemEntity itemEntity = new ItemEntity(this.level, this.worldPosition.getX() + 0.5f, this.worldPosition.getY() + 0.5f, this.worldPosition.getZ() + 0.5f, is);
                this.level.addFreshEntity(itemEntity);
            }

            this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
        }
    }

    private void loadData() {
        // The world can be null when loading the tile entity
        if (this.level == null)
            return;

        if (this.recipe == null && this.recipeId != null) {
            Map<ResourceLocation, MiniaturizationRecipe> loadedRecipes = RecipeHelper.getLoadedRecipes(this.level).stream()
                    .collect(Collectors.toMap(IRecipe::getId, Function.identity()));
            if (loadedRecipes.containsKey(this.recipeId)) {
                this.recipe = loadedRecipes.get(this.recipeId);
            } else {
                // The recipe has become invalid
                this.recipeId = null;
            }
        }

        if (this.masterProjector == null && this.masterPos != null) {
            TileEntity tile = this.level.getBlockEntity(this.masterPos);
            if (tile instanceof MainFieldProjectorTile) {
                this.masterProjector = (MainFieldProjectorTile) tile;
            } else {
                // The master projector has become invalid
                this.masterPos = null;
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);

        this.craftingProgress = compound.getInt("progress");

        if (compound.contains("recipe", Constants.NBT.TAG_STRING)) {
            this.recipeId = ResourceLocation.tryParse(compound.getString("recipe"));
            loadData();
        }

        if (compound.contains("masterPos", Constants.NBT.TAG_COMPOUND)) {
            masterPos = NBTUtil.readBlockPos(compound.getCompound("masterPos"));
            loadData();
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        compound.putInt("progress", this.craftingProgress);

        if (this.recipe != null)
            compound.putString("recipe", this.recipe.getId().toString());

        if (this.masterProjector != null && !this.masterProjector.isRemoved())
            compound.put("masterPos", NBTUtil.writeBlockPos(masterProjector.getBlockPos()));

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
