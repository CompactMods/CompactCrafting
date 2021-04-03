package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderState.DepthTestState;
import net.minecraft.client.renderer.RenderState.DiffuseLightingState;

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

    public static final RenderType PROJECTION_FIELD_RENDERTYPE = create("projection_field",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setTransparencyState(PROJECTION_TRANSPARENCY)
                    .setOutputState(RenderState.MAIN_TARGET)
                    .setCullState(RenderState.CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(DepthTestState.LEQUAL_DEPTH_TEST) // Default, but let's make sure it stays that way
                    .createCompositeState(false));

    public static final RenderType FIELD_PROJECTION_ARC = create("projection_field_arc",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setTransparencyState(PROJECTION_TRANSPARENCY)
                    .setOutputState(RenderState.MAIN_TARGET)
                    .setCullState(RenderState.NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(DepthTestState.LEQUAL_DEPTH_TEST) // Default, but let's make sure it stays that way
                    .createCompositeState(false));

    public static final RenderType MULTIBLOCK_GUI = create(CompactCrafting.MOD_ID + ":multiblock_gui",
            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setTransparencyState(PROJECTION_TRANSPARENCY)
                    .setOutputState(RenderState.MAIN_TARGET)
                    .setCullState(RenderState.CULL)
                    .setWriteMaskState(RenderState.COLOR_WRITE)
                    .setDiffuseLightingState(DiffuseLightingState.NO_DIFFUSE_LIGHTING)
                    .setDepthTestState(DepthTestState.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    public static IRenderTypeBuffer disableLighting(IRenderTypeBuffer in)
    {
        return wrapWithAdditional(
                in,
                "no_lighting",
                RenderSystem::disableLighting,
                () -> {
                }
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
