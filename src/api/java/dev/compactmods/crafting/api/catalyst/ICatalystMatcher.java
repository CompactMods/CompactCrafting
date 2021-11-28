package dev.compactmods.crafting.api.catalyst;

import net.minecraft.item.ItemStack;

public interface ICatalystMatcher {

    boolean matches(ItemStack stack);

    CatalystType<?> getType();
}
