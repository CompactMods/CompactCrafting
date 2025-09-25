package dev.compactmods.crafting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.crafting.client.render.CCRenderTypes;
import dev.compactmods.crafting.client.ClientPacketHandler;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.network.RequestProxyDataPacket;
import dev.compactmods.crafting.proxies.data.BaseFieldProxyEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyProjectorHighlighter {
    private static final Map<BlockPos, Long> blinkingProjectors = new ConcurrentHashMap<>();
    private static final long BLINK_DURATION = 10000;
    
    public static boolean addBlinkingProjector(BlockPos projectorPos) {
        long currentTime = System.currentTimeMillis();
        Long existingTime = blinkingProjectors.get(projectorPos);
        
        if (existingTime != null && (currentTime - existingTime) < BLINK_DURATION) {
            return false;
        }
        
        blinkingProjectors.put(projectorPos, currentTime);
        return true;
    }
    
    public static void renderProjectorHighlight(PoseStack poseStack, MultiBufferSource buffers, BlockPos proxyPos, Level level) {
        if (level == null) return;
        
        var blockEntity = level.getBlockEntity(proxyPos);
        if (blockEntity instanceof BaseFieldProxyEntity proxy) {
            ClientPacketHandler.validateAndGetProxyData(proxyPos, proxy.getProxyId());
        }
        
        if (ClientPacketHandler.isProxyDataStale(proxyPos)) {
            PacketDistributor.sendToServer(new RequestProxyDataPacket(proxyPos));
        }
        
        BlockPos fieldCenter = ClientPacketHandler.getProxyFieldCenter(proxyPos);
        if (fieldCenter == null) {
            return;
        }
        
        var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
        var fieldOpt = fields.get(fieldCenter);
        
        if (fieldOpt.isEmpty()) {
            return;
        }
        
        fieldOpt.ifPresent(field -> {
            Set<BlockPos> projectorPositions = new HashSet<>(field.getProjectors().locations());

            long currentTime = System.currentTimeMillis();
            
            for (BlockPos projectorPos : projectorPositions) {
                boolean shouldBlink = blinkingProjectors.containsKey(projectorPos);
                Long blinkStartTime = blinkingProjectors.get(projectorPos);
                
                if (shouldBlink && blinkStartTime != null && (currentTime - blinkStartTime) > BLINK_DURATION) {
                    blinkingProjectors.remove(projectorPos);
                    shouldBlink = false;
                }
                
                float alpha = 0.3f;
                if (shouldBlink && blinkStartTime != null) {
                    float blinkProgress = (currentTime - blinkStartTime) / 500.0f;
                    alpha = 0.2f + 0.4f * (float) Math.abs(Math.sin(blinkProgress));
                }
                
                renderProjectorOutline(poseStack, buffers, projectorPos, alpha);
            }
        });
    }
    
    private static void renderProjectorOutline(PoseStack poseStack, MultiBufferSource buffers, BlockPos projectorPos, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        poseStack.pushPose();
        poseStack.translate(
            projectorPos.getX() - cameraPos.x,
            projectorPos.getY() - cameraPos.y,
            projectorPos.getZ() - cameraPos.z
        );
        
        VertexConsumer consumer = buffers.getBuffer(CCRenderTypes.PROJECTOR_HIGHLIGHT);
        Matrix4f matrix = poseStack.last().pose();
        
        float red = 1.0f, green = 0.0f, blue = 0.0f;
        
        renderCubeFaces(consumer, matrix, red, green, blue, alpha);
        
        poseStack.popPose();
    }
    
    private static void renderCubeFaces(VertexConsumer consumer, Matrix4f matrix, float red, float green, float blue, float alpha) {
        float minX = -0.01f, minY = -0.01f, minZ = -0.01f;
        float maxX = 1.01f, maxY = 1.01f, maxZ = 1.01f;
        
        consumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        
        consumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        
        consumer.addVertex(matrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        
        consumer.addVertex(matrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        consumer.addVertex(matrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
    }
    
    public static void renderAllBlinkingProjectors(PoseStack poseStack, MultiBufferSource buffers, Level level) {
        if (level == null || blinkingProjectors.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        
        for (var entry : blinkingProjectors.entrySet()) {
            BlockPos projectorPos = entry.getKey();
            Long blinkStartTime = entry.getValue();
            
            if ((currentTime - blinkStartTime) <= BLINK_DURATION) {
                float blinkProgress = (currentTime - blinkStartTime) / 500.0f;
                float alpha = 0.2f + 0.4f * (float) Math.abs(Math.sin(blinkProgress));
                renderProjectorCube(poseStack, buffers, projectorPos, alpha);
            }
        }
    }
    
    private static void renderProjectorCube(PoseStack poseStack, MultiBufferSource buffers, BlockPos projectorPos, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        poseStack.pushPose();
        poseStack.translate(
            projectorPos.getX() - cameraPos.x,
            projectorPos.getY() - cameraPos.y,
            projectorPos.getZ() - cameraPos.z
        );
        
        VertexConsumer consumer = buffers.getBuffer(CCRenderTypes.PROJECTOR_HIGHLIGHT);
        Matrix4f matrix = poseStack.last().pose();
        
        float red = 1.0f, green = 0.0f, blue = 0.0f;
        
        renderCubeFaces(consumer, matrix, red, green, blue, alpha);
        
        poseStack.popPose();
    }

    public static void cleanupExpiredBlinking() {
        long currentTime = System.currentTimeMillis();
        blinkingProjectors.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > BLINK_DURATION);
    }

}