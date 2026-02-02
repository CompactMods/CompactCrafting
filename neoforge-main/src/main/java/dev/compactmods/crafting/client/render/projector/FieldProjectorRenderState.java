package dev.compactmods.crafting.client.render.projector;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class FieldProjectorRenderState extends BlockEntityRenderState {
    public int projectorColor;
    public Direction facing;
    public MiniaturizationFieldLocation fieldLocation;
    float gameTime;
}
