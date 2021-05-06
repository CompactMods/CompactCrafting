package com.robotgryphon.compactcrafting.inventory;

import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class MiniaturizationRecipeContainer extends Container implements IInventoryChangedListener {
    private final MiniaturizationRecipe recipe;
    private final Inventory container;
    private final boolean modifiable;
    private boolean copyValid = false;

    public MiniaturizationRecipeContainer(int id, PlayerInventory playerInv, PacketBuffer data) {
        this(id, playerInv, Registration.MINIATURIZATION_SERIALIZER.get().fromNetwork(data.readResourceLocation(), data), data.readBoolean());
    }

    public MiniaturizationRecipeContainer(int id, PlayerInventory playerInv, MiniaturizationRecipe recipe, boolean modifiable) {
        super(Registration.MINIATURIZATION_RECIPE_CONTAINER_TYPE.get(), id);
        this.recipe = recipe;
        this.container = new Inventory(5);
        this.modifiable = modifiable;
        this.container.addListener(this);

        if (modifiable)
            this.recipe.setId(null); // Remove id. We have to set one for networking purposes, but we can remove here.
        if (recipe.getCatalyst() != null)
            this.container.setItem(0, recipe.getCatalyst());
        List<ItemStack> outputs = recipe.getOutputs();
        int length = Math.min(4, outputs.size());
        for (int i = 0; i < length; i++) {
            this.container.setItem(i + 1, outputs.get(i));
        }

        this.addSlot(new ModifiableSlot(container, 0, 8, 114));
        for (int i = 0; i < 4; i++) {
            this.addSlot(new ModifiableSlot(container, i + 1, 98 + i * 18, 114));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 144 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 202));
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        if (!modifiable)
            return ItemStack.EMPTY;
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.container.stillValid(player);
    }

    @Override
    public void containerChanged(IInventory inv) {
        // We dot want to modify anything if modifiable is not true
        if (!this.modifiable)
            return;

        this.copyValid = recalculateCopyValid();
        recipe.setCatalyst(inv.getItem(0));
        for (int i = 0; i < 4; i++) {
            List<ItemStack> outputs = recipe.getOutputs();
            while (i >= outputs.size()) {
                outputs.add(ItemStack.EMPTY);
            }
            outputs.set(i, inv.getItem(i + 1));
        }
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        if (this.modifiable) {
            // Add back the items the player used when making the recipe
            int size = this.container.getContainerSize();
            for (int i = 0; i < size; i++) {
                player.inventory.add(this.container.getItem(i));
            }
        }
    }

    public MiniaturizationRecipe getRecipe() {
        return recipe;
    }

    public Inventory getInnerInventory() {
        return container;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public boolean isCopyValid() {
        return copyValid;
    }

    /**
     * Recalculate whether the copy button is valid and should be enabled based on if there is a non-empty catalyst and at least one output.
     *
     * @return
     */
    private boolean recalculateCopyValid() {
        if (container.getItem(0) == ItemStack.EMPTY)
            return false;

        for (int i = 1; i < container.getContainerSize(); i++) {
            if (container.getItem(i) != ItemStack.EMPTY)
                return true;
        }

        return false;
    }

    private class ModifiableSlot extends Slot {
        ModifiableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPickup(PlayerEntity playerIn) {
            return isModifiable();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isModifiable();
        }
    }
}
