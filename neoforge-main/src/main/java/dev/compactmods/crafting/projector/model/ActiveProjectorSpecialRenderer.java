package dev.compactmods.crafting.projector.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class ActiveProjectorSpecialRenderer implements NoDataSpecialModelRenderer {
    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        submitNodeCollector.submitNameTag(poseStack, null, 10,
                Component.literal("Hello there!"), true,
                lightCoords, 0, new CameraRenderState());
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {

    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {

        public static final MapCodec<ActiveProjectorSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new ActiveProjectorSpecialRenderer.Unbaked());

        @Override
        public SpecialModelRenderer<Void> bake(BakingContext context) {
            return new ActiveProjectorSpecialRenderer();
        }

        @Override
        public MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
