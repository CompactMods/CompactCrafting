package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public class RenderTypesExtensions extends RenderType {
    // Dummy
    public RenderTypesExtensions(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    /**
     * Sets up a transparency state based on original GL calls from Compact Machines. (1.12.x)
     */
    protected static final RenderState.TransparencyState PROJECTION_TRANSPARENCY = new RenderState.TransparencyState("projection_field_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static final RenderType PROJECTION_FIELD_RENDERTYPE = makeType("projection_field",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .transparency(PROJECTION_TRANSPARENCY)
                    .target(RenderState.MAIN_TARGET)
                    .cull(RenderState.CULL_ENABLED)
                    .writeMask(COLOR_WRITE)
                    .depthTest(DepthTestState.DEPTH_LEQUAL) // Default, but let's make sure it stays that way
                    .build(false));

    public static final RenderType FIELD_PROJECTION_ARC = makeType("projection_field_arc",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .transparency(PROJECTION_TRANSPARENCY)
                    .target(RenderState.MAIN_TARGET)
                    .cull(RenderState.CULL_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .depthTest(DepthTestState.DEPTH_LEQUAL) // Default, but let's make sure it stays that way
                    .build(false));
}
