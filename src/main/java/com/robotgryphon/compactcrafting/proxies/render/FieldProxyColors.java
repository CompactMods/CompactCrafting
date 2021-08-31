package com.robotgryphon.compactcrafting.proxies.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class FieldProxyColors {
    private static final int MATCH = 0xFF319a3b;
    private static final int RESCAN = 0xFFf062de;
    public static class MatchBlock implements IBlockColor {
        @Override
        public int getColor(BlockState state, @Nullable IBlockDisplayReader level, @Nullable BlockPos pos, int tintIndex) {
            return MATCH;
        }
    }

    public static class MatchItem implements IItemColor {
        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            return MATCH;
        }
    }

    public static class RescanBlock implements IBlockColor {
        @Override
        public int getColor(BlockState state, @Nullable IBlockDisplayReader level, @Nullable BlockPos pos, int tintIndex) {
            return RESCAN;
        }
    }

    public static class RescanItem implements IItemColor {
        @Override
        public int getColor(ItemStack p_getColor_1_, int p_getColor_2_) {
            return RESCAN;
        }
    }
}
