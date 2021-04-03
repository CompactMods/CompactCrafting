package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.blocks.MainFieldProjectorTile;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(BlockPos center, FieldProjectionSize fieldSize) {
        BlockPos mainProjector = ProjectorHelper.getProjectorLocationForDirection(center, Direction.NORTH, fieldSize);

        FieldProjection fp = FieldProjection.fromSizeAndCenter(fieldSize, center);
        Minecraft mc = Minecraft.getInstance();
        mc.deferTask(() -> {
            World w = Minecraft.getInstance().world;
            MainFieldProjectorTile mainTile = (MainFieldProjectorTile) w.getTileEntity(mainProjector);
            if (mainTile == null)
                return;

            mainTile.setFieldInfo(fp);
        });
    }

    public static void handleFieldDeactivation(BlockPos center, FieldProjectionSize fieldSize) {
        BlockPos mainProjector = ProjectorHelper.getProjectorLocationForDirection(center, Direction.NORTH, fieldSize);

        World w = Minecraft.getInstance().world;
        MainFieldProjectorTile mainTile = (MainFieldProjectorTile) w.getTileEntity(mainProjector);
        if (mainTile == null)
            return;

        mainTile.invalidateField();
    }
}
