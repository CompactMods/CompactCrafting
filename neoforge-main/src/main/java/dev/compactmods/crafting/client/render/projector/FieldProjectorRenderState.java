package dev.compactmods.crafting.client.render.projector;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class FieldProjectorRenderState extends BlockEntityRenderState {
    public int projectorColor;
    public Direction facing;
    public MiniaturizationFieldLocation fieldLocation;
    public BlockModelRenderState dishRenderState;
    public List<BlockStateModelPart> dishModelParts;
    float gameTime;
}
