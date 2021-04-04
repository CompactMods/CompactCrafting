package com.robotgryphon.compactcrafting.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class CCRenderTypes {

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

    public static final RenderType PROJECTION_FIELD_RENDERTYPE = RenderType.create("projection_field",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setTransparencyState(PROJECTION_TRANSPARENCY)
                    .setCullState(new RenderState.CullState(true))
                    .setWriteMaskState(new RenderState.WriteMaskState(true, false))
                    .createCompositeState(false));

//    public static final RenderType FIELD_PROJECTION_ARC = create("projection_field_arc",
//            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
//            RenderType.State.builder()
//                    .setTransparencyState(PROJECTION_TRANSPARENCY)
//                    .setOutputState(RenderState.MAIN_TARGET)
//                    .setCullState(RenderState.NO_CULL)
//                    .setWriteMaskState(COLOR_WRITE)
//                    .setDepthTestState(DepthTestState.LEQUAL_DEPTH_TEST) // Default, but let's make sure it stays that way
//                    .createCompositeState(false));
//
//    public static final RenderType MULTIBLOCK_GUI = create(CompactCrafting.MOD_ID + ":multiblock_gui",
//            DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256,
//            RenderType.State.builder()
//                    .setTransparencyState(PROJECTION_TRANSPARENCY)
//                    .setOutputState(RenderState.MAIN_TARGET)
//                    .setCullState(RenderState.CULL)
//                    .setWriteMaskState(RenderState.COLOR_WRITE)
//                    .setDiffuseLightingState(DiffuseLightingState.NO_DIFFUSE_LIGHTING)
//                    .setDepthTestState(DepthTestState.LEQUAL_DEPTH_TEST)
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
