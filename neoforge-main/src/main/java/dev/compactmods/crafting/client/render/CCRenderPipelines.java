package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.client.renderer.RenderPipelines;

public class CCRenderPipelines {
    public static final RenderPipeline FIELD_OUTLINE_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(CompactCrafting.identifier("pipeline/lines_translucent"))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .build();

    public static final RenderPipeline FIELD_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(CompactCrafting.identifier("projection_field"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    /// The pipeline for projector scanning geometry
    public static final RenderPipeline PROJECTOR_SCAN_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(CompactCrafting.identifier("projector_scan"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
}
