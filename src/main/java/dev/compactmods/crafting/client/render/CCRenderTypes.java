package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CCRenderTypes {

    public static final RenderType FIELD_RENDER_TYPE = RenderType.create("projection_field",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(true))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    public static final RenderType PHANTOM = RenderType.create("phantom", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.ShaderStateShard.BLOCK_SHADER)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(true));
}
