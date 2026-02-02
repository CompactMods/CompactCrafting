package dev.compactmods.crafting.client.render;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class CCRenderTypes {

    public static final RenderType FIELD_OUTLINE = RenderType.create("projection_field_outline", RenderSetup
            .builder(CCRenderPipelines.FIELD_OUTLINE_PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup());

    public static final RenderType FIELD = RenderType.create("projection_field", RenderSetup
            .builder(CCRenderPipelines.FIELD_PIPELINE)
            .createRenderSetup());

    public static final RenderType PROJECTOR_SCAN = RenderType.create("projector_scan", RenderSetup
            .builder(CCRenderPipelines.PROJECTOR_SCAN_PIPELINE)
            .createRenderSetup());
}
