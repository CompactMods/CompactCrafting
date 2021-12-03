package dev.compactmods.crafting.client.render;

import java.util.OptionalDouble;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CCRenderTypes extends RenderType {

    public static final RenderType LINE_STRIP = create("line_strip",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINE_STRIP, 256, false, false,
            RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(4f)))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    public static final RenderType FIELD_RENDER_TYPE = create("projection_field",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 262144, false, true,
            RenderType.CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.of(4f)))
                    .setLightmapState(LIGHTMAP)
                    .setShaderState(ShaderStateShard.POSITION_COLOR_SHADER)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(true));

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
