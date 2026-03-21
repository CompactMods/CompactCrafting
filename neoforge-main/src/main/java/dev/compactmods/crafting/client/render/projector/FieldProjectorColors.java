package dev.compactmods.crafting.client.render.projector;

import dev.compactmods.crafting.client.ClientConfig;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldProjectorColors {

    public static class Block implements BlockTintSource {

        @Override
        public int color(BlockState state) {
            return ClientConfig.projectorColor;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return ClientConfig.projectorColor;
        }
    }

    public static class Item {

        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 0) return 0;

            return ClientConfig.projectorOffColor;
        }
    }
}
