package dev.compactmods.crafting.client;

import javax.annotation.Nullable;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.projector.block.FieldProjectorBlock;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(BlockPos[] projectorLocations, MiniaturizationFieldSize fieldSize) {
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
