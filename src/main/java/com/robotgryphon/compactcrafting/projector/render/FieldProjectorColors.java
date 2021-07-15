package com.robotgryphon.compactcrafting.projector.render;

import com.robotgryphon.compactcrafting.client.ClientConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class FieldProjectorColors {

    public static class Block implements IBlockColor {

        @Override
        public int getColor(BlockState state, @Nullable IBlockDisplayReader level, @Nullable BlockPos pos, int tintIndex) {
            switch(tintIndex) {
                case 0:
                    return ClientConfig.projectorOffColor;

                case 1:
                    return 0xFF2494cd;
            }

            return 0x00000000;
        }
    }

    public static class Item implements IItemColor {
        @Override
        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 0) return 0;

            return ClientConfig.projectorOffColor;
        }
    }
}
