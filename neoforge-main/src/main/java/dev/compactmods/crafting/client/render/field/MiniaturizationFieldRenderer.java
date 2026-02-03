package dev.compactmods.crafting.client.render.field;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.client.ClientConfig;
import dev.compactmods.crafting.client.render.MiniaturizationFieldRenderBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class MiniaturizationFieldRenderer {

    public static void onRenderStage(RenderLevelStageEvent.AfterParticles evt) {
        final var mc = Minecraft.getInstance();
        final var level = mc.level;

        if (level == null) return;

        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        // FIXME: Field rendering
//        level.getExistingData(MiniaturizationFields.ACTIVE_FIELDS).ifPresent(fields -> {
//            fields.stream().forEach(field -> {
//                render(field, evt.getPoseStack(), buffers);
//            });
//        });
    }

    public static void render(IMiniaturizationField field, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        final Minecraft mc = Minecraft.getInstance();
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        Vec3 projectedView = mainCamera.position();

        pose.pushPose();
        {
            pose.translate(-projectedView.x, -projectedView.y, -projectedView.z);
//            if(field.() == EnumCraftingState.CRAFTING) {
//                CraftingPreviewRenderer.render(projectors.currentRecipe(), projectors.getProgress(), pose, buffers, 0, 0);
//            }

            final var renderInstructions = MiniaturizationFieldRenderBuilder
                    .forDefault(ClientConfig.projectorColor)
                    .withFieldBoundaries(field.location().bounds())
                    .withProjectors(field.getProjectors())
                    .build();

            for(final var inst : renderInstructions) {
                inst.draw(pose, buffers);
            }
        }
        pose.popPose();
    }
}
