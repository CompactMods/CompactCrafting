package dev.compactmods.crafting.api.projector;

import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class FieldProjectorProperties {

    public static final EnumProperty<MiniaturizationFieldSize> SIZE = EnumProperty.create("field", MiniaturizationFieldSize.class);
    
}
