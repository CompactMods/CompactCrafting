package com.robotgryphon.compactcrafting.projector.render;

import com.robotgryphon.compactcrafting.client.ClientConfig;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class FieldProjectorColors implements IBlockColor {
    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader levelReader, @Nullable BlockPos pos, int tintIndex) {
        if (!(state.getBlock() instanceof FieldProjectorBlock))
            return 0;

        boolean active = FieldProjectorBlock.isActive(state);
        if (active) return 0;

        if (tintIndex != 0) return 0;

        return ClientConfig.projectorOffColor;
    }
}
