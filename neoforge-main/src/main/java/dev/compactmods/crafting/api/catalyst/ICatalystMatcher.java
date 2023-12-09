package dev.compactmods.crafting.api.catalyst;

import dev.compactmods.crafting.api.catalyst.CatalystType;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public interface ICatalystMatcher {

    boolean matches(ItemStack stack);

    CatalystType<?> getType();

    Set<ItemStack> getPossible();
}
