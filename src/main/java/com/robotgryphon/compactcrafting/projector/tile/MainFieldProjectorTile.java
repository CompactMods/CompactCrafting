package com.robotgryphon.compactcrafting.projector.tile;

import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.util.math.AxisAlignedBB;

public class MainFieldProjectorTile extends FieldProjectorTile {

    public MainFieldProjectorTile() {
        super(Registration.MAIN_FIELD_PROJECTOR_TILE.get());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // Check - if we have a valid field use the entire field plus space
        // Otherwise just use the super implementation
        return fieldCap
                .map(field -> field.getBounds().inflate(10))
                .orElse(super.getRenderBoundingBox());
    }
}
