package com.robotgryphon.compactcrafting.projector.render;

import com.robotgryphon.compactcrafting.client.ClientConfig;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class FieldProjectorColors implements IItemColor {

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return 0;

        return ClientConfig.projectorOffColor;
    }
}
