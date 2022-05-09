package dev.compactmods.crafting.client;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.field.ProjectorRenderStyle;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.projector.IProjectorRenderInfo;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.field.render.CraftingPreviewRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onTick(final TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START) return;

        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(CCCapabilities.TEMP_PROJECTOR_RENDERING)
                    .ifPresent(IProjectorRenderInfo::tick);
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && !Minecraft.getInstance().isPaused()) {
            level.getCapability(CCCapabilities.FIELDS)
                    .ifPresent(IActiveWorldFields::tickFields);
        }
    }

    @SubscribeEvent
    public static void onWorldRender(final RenderLevelLastEvent event) {
        final Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
            return;

        doProjectorRender(event, mc);
        doFieldPreviewRender(event, mc);
    }

    @Nonnull
    private static void doFieldPreviewRender(RenderLevelLastEvent event, Minecraft mc) {
        final Camera mainCamera = mc.gameRenderer.getMainCamera();
        final HitResult hitResult = mc.hitResult;

        double viewDistance = 64;
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        mc.level.getCapability(CCCapabilities.FIELDS)
                .ifPresent(fields -> {
                    fields.getFields()
                            .filter(field -> Vec3.atCenterOf(field.getCenter()).closerThan(mainCamera.getPosition(), viewDistance))
                            .filter(field -> field.getCraftingState() == EnumCraftingState.CRAFTING)
                            .filter(field -> field.getCurrentRecipe().isPresent())
                            .filter(field -> !(field instanceof ProjectorRenderStyle rcf) || rcf.enabled())
                            .forEach(field -> {
                                BlockPos center = field.getCenter();

                                PoseStack stack = event.getPoseStack();
                                stack.pushPose();
                                Vec3 projectedView = mainCamera.getPosition();
                                stack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                                stack.translate(center.getX(), center.getY(), center.getZ());

                                final IMiniaturizationRecipe rec = field.getCurrentRecipe().get();
                                final int craftingProgress = field.getProgress();

                                CraftingPreviewRenderer.render(
                                        rec, craftingProgress, stack,
                                        buffers, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY
                                );

                                stack.popPose();
                            });
                });
        buffers.endBatch();
    }

    private static void doProjectorRender(RenderLevelLastEvent event, Minecraft mc) {
        mc.player.getCapability(CCCapabilities.TEMP_PROJECTOR_RENDERING)
                .ifPresent(render -> render.render(event.getPoseStack()));
    }
}
