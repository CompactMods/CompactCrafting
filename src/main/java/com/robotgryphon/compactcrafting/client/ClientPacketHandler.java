package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.MiniaturizationField;
import com.robotgryphon.compactcrafting.field.capability.CapabilityActiveWorldFields;
import com.robotgryphon.compactcrafting.field.capability.IMiniaturizationField;
import com.robotgryphon.compactcrafting.projector.ProjectorHelper;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(BlockPos center, FieldProjectionSize fieldSize) {
        MiniaturizationField fp = MiniaturizationField.fromSizeAndCenter(fieldSize, center);
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                    .ifPresent(fields -> {
                        fields.activateField(fp);
                        LazyOptional<IMiniaturizationField> lazy = fields.getLazy(fp.getCenterPosition());
                        ProjectorHelper.getProjectorLocations(fp.getCenterPosition(), fp.getFieldSize())
                                .forEach(projectorPos -> {
                                    FieldProjectorTile tile = (FieldProjectorTile) mc.level.getBlockEntity(projectorPos);
                                    if(tile == null)
                                        return;

                                    tile.setField(lazy);
                                });
                    });
        });
    }

    public static void handleFieldDeactivation(BlockPos center, FieldProjectionSize fieldSize) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            mc.level.getCapability(CapabilityActiveWorldFields.ACTIVE_WORLD_FIELDS)
                .ifPresent(fields -> {
                    fields.getLazy(center).invalidate();
                });
        });
    }
}
