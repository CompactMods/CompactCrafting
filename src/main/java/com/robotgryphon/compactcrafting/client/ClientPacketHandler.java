package com.robotgryphon.compactcrafting.client;

import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(BlockPos[] projectorLocations, FieldProjectionSize fieldSize) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientWorld cw = mc.level;
            for(BlockPos proj : projectorLocations) {
                if(cw.getBlockState(proj).getBlock() instanceof FieldProjectorBlock) {
                    FieldProjectorBlock.activateProjector(cw, proj, fieldSize);
                }
            }
        });
    }

    public static void handleFieldDeactivation(BlockPos[] projectorLocations) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientWorld cw = mc.level;
            for(BlockPos proj : projectorLocations) {
                if(cw.getBlockState(proj).getBlock() instanceof FieldProjectorBlock) {
                    FieldProjectorBlock.deactivateProjector(cw, proj);
                }
            }
        });
    }
}
