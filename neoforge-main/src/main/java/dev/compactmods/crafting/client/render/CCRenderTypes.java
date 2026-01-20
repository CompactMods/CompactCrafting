package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.compactmods.crafting.api.CompactCrafting;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;

public class CCRenderTypes {

    public static final RenderPipeline FIELD_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(CompactCrafting.identifier("projection_field"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderType FIELD_RENDER_TYPE = RenderType.create("projection_field", RenderSetup
            .builder(FIELD_PIPELINE)
            .setOutputTarget(OutputTarget.MAIN_TARGET)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setTextureTransform(TextureTransform.DEFAULT_TEXTURING)
            .sortOnUpload()
            .createRenderSetup());

}
//                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
//                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                    .setCullState(new RenderStateShard.CullStateShard(false))
//                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
//                    .createCompositeState(false));

//    public static final RenderType PHANTOM = RenderType.create("phantom", DefaultVertexFormat.BLOCK,
//            VertexFormat.Mode.QUADS, 2097152,
//            true, false,
//            RenderSetup.builder(RenderPipelines.TRANSLUCENT_MOVING_BLOCK)
//                .setShaderState(BLOCK_SHADER)
//                .setLightmapState(RenderStateShard.LIGHTMAP)
//                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
//                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                .createCompositeState(true));
