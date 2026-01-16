package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;

public class CCRenderTypes {

    public static final RenderType FIELD_RENDER_TYPE = RenderType.create("projection_field", RenderSetup
            .builder(RenderPipelines.TRANSLUCENT_MOVING_BLOCK)
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
