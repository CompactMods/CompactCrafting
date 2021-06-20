package com.robotgryphon.compactcrafting.projector.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class CCRenderTypes extends RenderType {

    public CCRenderTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_) {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static final RenderState.TransparencyState FIELD_TRANSPARENCY = new RenderState.TransparencyState("field_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static final RenderType FIELD_RENDER_TYPE = create("projection_field",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 512,
            RenderType.State.builder()
                    .setLightmapState(NO_LIGHTMAP)
                    .setLayeringState(RenderState.POLYGON_OFFSET_LAYERING)
                    .setTransparencyState(FIELD_TRANSPARENCY)
                    .setDepthTestState(RenderState.LEQUAL_DEPTH_TEST)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(RenderState.NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

//    public static final RenderType FIELD_PROJECTION_ARC = RenderType.create("projection_field_arc",
//            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
//            RenderType.State.builder()
//                    .setTransparencyState(PROJECTION_TRANSPARENCY)
//                    .setCullState(new RenderState.CullState(false))
//                    .setWriteMaskState(new RenderState.WriteMaskState(true, true))
//                    .createCompositeState(false));



    public static IRenderTypeBuffer disableLighting(IRenderTypeBuffer.Impl in)
    {
        return wrapWithAdditional(
                in,
                "no_lighting",
                RenderSystem::disableLighting,
                RenderSystem::enableLighting
        );
    }

    private static IRenderTypeBuffer wrapWithAdditional(
            IRenderTypeBuffer in,
            String name,
            Runnable setup,
            Runnable teardown
    )
    {
        return type -> in.getBuffer(new RenderType(
                CompactCrafting.MOD_ID+":"+type+"_"+name,
                type.format(),
                type.mode(),
                type.bufferSize(),
                type.affectsCrumbling(),
                false, // needsSorting is private and shouldn't be too relevant here
                () -> {
                    type.setupRenderState();
                    setup.run();
                },
                () -> {
                    teardown.run();
                    type.clearRenderState();
                }
        )
        {
        });
    }
}
