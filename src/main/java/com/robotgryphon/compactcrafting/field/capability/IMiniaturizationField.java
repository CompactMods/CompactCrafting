package com.robotgryphon.compactcrafting.field.capability;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface IMiniaturizationField {

    AxisAlignedBB getBounds();

    FieldProjectionSize getFieldSize();

    BlockPos getCenterPosition();
}
