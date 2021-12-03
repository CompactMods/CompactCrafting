package dev.compactmods.crafting.api.catalyst;

import java.util.Set;
import net.minecraft.world.item.ItemStack;

public interface ICatalystMatcher {

    boolean matches(ItemStack stack);

    CatalystType<?> getType();

    Set<ItemStack> getPossible();
}
