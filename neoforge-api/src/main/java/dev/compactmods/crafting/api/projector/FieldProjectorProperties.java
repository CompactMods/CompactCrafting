package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class FieldProjectorProperties {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final EnumProperty<MiniaturizationFieldSize> SIZE = EnumProperty.create("field", MiniaturizationFieldSize.class);
    
}
