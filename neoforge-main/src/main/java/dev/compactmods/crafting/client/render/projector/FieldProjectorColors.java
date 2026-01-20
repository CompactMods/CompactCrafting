package dev.compactmods.crafting.client.render.projector;

import dev.compactmods.crafting.client.ClientConfig;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldProjectorColors {

    public static class Block implements BlockColor {

        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            switch(tintIndex) {
                case 0:
                    return ClientConfig.projectorOffColor;

                case 1:
                    return 0xFF2494cd;
            }

            return 0x00000000;
        }
    }

    public static class Item {

        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 0) return 0;

            return ClientConfig.projectorOffColor;
        }
    }
}
