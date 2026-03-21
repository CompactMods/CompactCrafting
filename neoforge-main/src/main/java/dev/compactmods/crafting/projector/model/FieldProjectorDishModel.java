package dev.compactmods.crafting.projector.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class FieldProjectorDishModel extends Model<ProjectorDishRenderState> {

    public FieldProjectorDishModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutCull);
    }
}
