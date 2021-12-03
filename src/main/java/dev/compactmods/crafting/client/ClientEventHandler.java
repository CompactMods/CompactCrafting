package dev.compactmods.crafting.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.capability.CapabilityProjectorRenderInfo;
import dev.compactmods.crafting.capability.IProjectorRenderInfo;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.field.render.CraftingPreviewRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        final ClientPlayerEntity player = Minecraft.getInstance().player;
        if(player != null) {
            player.getCapability(CapabilityProjectorRenderInfo.TEMP_PROJECTOR_RENDERING)
                    .ifPresent(IProjectorRenderInfo::tick);
        }

        ClientWorld level = Minecraft.getInstance().level;
        if (level != null && !Minecraft.getInstance().isPaused()) {
            level.getCapability(CapabilityActiveWorldFields.FIELDS)
                    .ifPresent(IActiveWorldFields::tickFields);
        }
    }

    @SubscribeEvent
    public static void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
            return;

        mc.player.getCapability(CapabilityProjectorRenderInfo.TEMP_PROJECTOR_RENDERING)
                .ifPresent(render -> render.render(event.getMatrixStack()));

        final ActiveRenderInfo mainCamera = mc.gameRenderer.getMainCamera();
        final RayTraceResult hitResult = mc.hitResult;

        double viewDistance = 64;
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        mc.level.getCapability(CapabilityActiveWorldFields.FIELDS)
                .ifPresent(fields -> {
                    fields.getFields()
                            .filter(field -> Vector3d.atCenterOf(field.getCenter()).closerThan(mainCamera.getPosition(), viewDistance))
                            .filter(field -> field.getCraftingState() == EnumCraftingState.CRAFTING)
                            .filter(field -> field.getCurrentRecipe().isPresent())
                            .filter(IMiniaturizationField::enabled)
                            .forEach(field -> {
                                BlockPos center = field.getCenter();

                                MatrixStack stack = event.getMatrixStack();
                                stack.pushPose();
                                Vector3d projectedView = mainCamera.getPosition();
                                stack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                                stack.translate(
                                        (double) center.getX(),
                                        (double) center.getY(),
                                        (double) center.getZ()
                                );

                                CraftingPreviewRenderer.render(
                                        field.getCurrentRecipe().get(), field.getProgress(), stack,
                                        buffers, 15728880, OverlayTexture.NO_OVERLAY
                                );

                                stack.popPose();
                            });
                });

        buffers.endBatch();
    }
}
