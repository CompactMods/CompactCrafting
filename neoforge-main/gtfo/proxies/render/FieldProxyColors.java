package dev.compactmods.crafting.proxies.render;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import org.jetbrains.annotations.Nullable;

public class FieldProxyColors {
    private static final int MATCH = 0xFF319a3b;
    private static final int RESCAN = 0xFFf062de;
    public static class MatchBlock implements BlockColor {
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            return MATCH;
        }
    }

    public static class MatchItem implements ItemColor {
        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            return MATCH;
        }
    }

    public static class RescanBlock implements BlockColor {
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            return RESCAN;
        }
    }

    public static class RescanItem implements ItemColor {
        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            return RESCAN;
        }
    }
}
