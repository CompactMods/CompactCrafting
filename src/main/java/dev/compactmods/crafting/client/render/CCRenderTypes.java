package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CCRenderTypes extends RenderType {
    protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("cc_translucent", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static final RenderType FIELD_RENDER_TYPE = create("projection_field",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, RenderType.MEDIUM_BUFFER_SIZE, false, true,
            RenderType.CompositeState.builder()
                    .setTransparencyState(new TransparencyStateShard("transparent", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.enableDepthTest();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    }, () -> {
                        RenderSystem.disableDepthTest();
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }))
                    .setTextureState(NO_TEXTURE)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setOutputState(RenderType.OUTLINE_TARGET)
                    .createCompositeState(true));

//                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
//                    .setWriteMaskState(COLOR_DEPTH_WRITE)
//                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                    .setOutputState(OutputStateShard.TRANSLUCENT_TARGET)
//                    .setCullState(RenderStateShard.NO_CULL)
//                    .createCompositeState(false));

    public static final RenderType PHANTOM = create("phantom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder()
            .setShaderState(ShaderStateShard.BLOCK_SHADER)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(true));

    public CCRenderTypes(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
        super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
    }
}
