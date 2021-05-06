package com.robotgryphon.compactcrafting.crafting;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;

public class CraftingHelper {
    private CraftingHelper() {}

    /**
     * Consumes a number of items from a stack in the world.
     *
     * @param item The item entity (stack) to remove items from.
     * @param count The number of items to remove.
     * @return True if there was enough items to remove; false otherwise.
     */
    public static boolean consumeCatalystItem(ItemEntity item, int count) {
        ItemStack stack = item.getItem();

        // Not enough items (tm?)
        if(stack.getCount() < count)
            return false;

        // Delete the item entity; stack had exactly enough
        if(stack.getCount() == count) {
            item.remove();
            return true;
        }

        // Remove items from stack and clone the entity
        stack.setCount(stack.getCount() - count);
        item.setItem(stack.copy());
        return true;
    }

//    public static void setCraftingHologram(ServerWorld world, BlockPos center) {
//        // Create recipe hologram
//        world.setBlockState(center, Blockss.craftingHologram.getDefaultState());
//        TileEntityCraftingHologram teHologram = (TileEntityCraftingHologram) world.getTileEntity(center);
//        if(teHologram != null) {
//            teHologram.setRecipe(multiblockRecipe);
//        }
//    }
}
