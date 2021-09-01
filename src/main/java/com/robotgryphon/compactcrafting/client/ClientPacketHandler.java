package com.robotgryphon.compactcrafting.client;

import javax.annotation.Nullable;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(BlockPos[] projectorLocations, FieldProjectionSize fieldSize) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientWorld cw = mc.level;
            for (BlockPos proj : projectorLocations) {
                if (cw.getBlockState(proj).getBlock() instanceof FieldProjectorBlock) {
                    FieldProjectorBlock.activateProjector(cw, proj, fieldSize);
                }
            }
        });
    }

    public static void handleFieldDeactivation(BlockPos[] projectorLocations) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientWorld cw = mc.level;
            for (BlockPos proj : projectorLocations) {
                if (cw.getBlockState(proj).getBlock() instanceof FieldProjectorBlock) {
                    FieldProjectorBlock.deactivateProjector(cw, proj);
                }
            }
        });
    }

    public static void handleFieldData(CompoundNBT fieldData) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        MiniaturizationField field = new MiniaturizationField();
        field.setLevel(mc.level);
        field.loadClientData(fieldData);

        mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.setLevel(mc.level);
                    fields.registerField(field);
                });
    }

    public static void removeField(BlockPos fieldCenter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.unregisterField(fieldCenter);
                });
    }

    public static void handleRecipeChanged(BlockPos center, @Nullable ResourceLocation recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .resolve()
                .flatMap(af -> af.get(center))
                .ifPresent(field -> field.setRecipe(recipe));
    }
}
