package dev.compactmods.crafting.client;

import javax.annotation.Nullable;
import java.util.Objects;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.field.capability.CapabilityActiveWorldFields;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(IMiniaturizationField field, CompoundNBT fieldClientData) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientWorld cw = mc.level;
            if (cw == null)
                return;

            field.setLevel(cw);
            field.loadClientData(fieldClientData);

            mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .ifPresent(fields -> fields.registerField(field));

            field.getProjectorPositions().forEach(proj -> {
                if (cw.getBlockState(proj).getBlock() instanceof FieldProjectorBlock) {
                    FieldProjectorBlock.activateProjector(cw, proj, field.getFieldSize());
                    ((FieldProjectorTile) cw.getBlockEntity(proj)).updateFieldInfo();
                }
            });

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

                    field.getProjectorPositions()
                            .map(mc.level::getBlockEntity)
                            .map(tile -> (FieldProjectorTile) tile)
                            .filter(Objects::nonNull)
                            .forEach(FieldProjectorTile::updateFieldInfo);
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
