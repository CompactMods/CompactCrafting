package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.field.IMutableMiniaturizationField;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.network.FieldActivatedPacket;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ClientPacketHandler {
    
    private static final Map<BlockPos, ProxyData> clientProxyDataMap = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 1000;
    
    private static record ProxyData(@Nullable BlockPos fieldCenter, UUID proxyId, long timestamp) {}

    public static void handleFieldActivation(IMiniaturizationField<MiniaturizationRecipe> field, CompoundTag fieldClientData) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            if (cw == null)
                return;

            mc.level.getData(CCAttachments.ACTIVE_FIELDS).registerField(field);
        });
    }

    public static void handleFieldDeactivation(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        mc.submitAsync(() -> {
            mc.level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.get(center).ifPresent(field -> {
                    final var projectors = field.getProjectors();
                    projectors.disableAll();
                });

                fields.unregisterField(center);
            });
        });
    }

    public static void handleFieldData(CompoundTag fieldData) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        MiniaturizationField field = MiniaturizationField.fromNBT(mc.level, fieldData);
        mc.level.getData(CCAttachments.ACTIVE_FIELDS).registerField(field);
    }

    public static void removeField(BlockPos fieldCenter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS).unregisterField(fieldCenter);
    }

    public static void clearFieldRecipe(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS)
                .get(center)
                .filter(field -> field instanceof IMutableMiniaturizationField)
                .map(IMutableMiniaturizationField.class::cast)
                .ifPresent(IMutableMiniaturizationField::clearRecipe);
    }

    public static void changeFieldRecipe(BlockPos center, RecipeHolder<MiniaturizationRecipe> recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS)
                .get(center)
                .filter(field -> field instanceof IMutableMiniaturizationField)
                .map(IMutableMiniaturizationField.class::cast)
                .ifPresent(field -> {
                    field.setRecipe(recipe);
                    field.spawnParticlesAtProjectors(MiniaturizationField.RECIPE_MATCHED_PARTICLE_OPTS);
                });

    }

    public static FieldActivatedPacket createFieldActivationPacket(MiniaturizationFieldSize fieldSize, BlockPos center, CompoundTag clientData) {
        var field = MiniaturizationField.fromNBT(Minecraft.getInstance().level, clientData);
        return new FieldActivatedPacket(field, clientData);
    }
    
    public static void handleProxyData(BlockPos proxyPos, @Nullable BlockPos fieldCenter, UUID proxyId) {
        clientProxyDataMap.put(proxyPos, new ProxyData(fieldCenter, proxyId, System.currentTimeMillis()));
    }
    
    public static BlockPos getProxyFieldCenter(BlockPos proxyPos) {
        ProxyData data = clientProxyDataMap.get(proxyPos);
        return data != null ? data.fieldCenter : null;
    }
    
    public static boolean isProxyDataStale(BlockPos proxyPos) {
        ProxyData data = clientProxyDataMap.get(proxyPos);
        if (data == null) return true;
        return System.currentTimeMillis() - data.timestamp > CACHE_DURATION;
    }
    
    public static void removeProxyData(BlockPos proxyPos) {
        clientProxyDataMap.remove(proxyPos);
    }
    
    public static void validateAndGetProxyData(BlockPos proxyPos, UUID currentProxyId) {
        ProxyData data = clientProxyDataMap.get(proxyPos);
        if (data != null && !data.proxyId.equals(currentProxyId)) {
            clientProxyDataMap.remove(proxyPos);
        }
    }
}
