package com.robotgryphon.compactcrafting.client;

import com.robotgryphon.compactcrafting.client.sound.MiniaturizationSound;
import com.robotgryphon.compactcrafting.field.FieldProjection;
import com.robotgryphon.compactcrafting.field.FieldProjectionSize;
import com.robotgryphon.compactcrafting.field.ProjectorHelper;
import com.robotgryphon.compactcrafting.tiles.MainFieldProjectorTile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ClientPacketHandler {
    public static void handleFieldActivation(BlockPos center, FieldProjectionSize fieldSize) {
        BlockPos mainProjector = ProjectorHelper.getProjectorLocationForDirection(center, Direction.NORTH, fieldSize);

        FieldProjection fp = FieldProjection.fromSizeAndCenter(fieldSize, center);
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            World w = Minecraft.getInstance().level;
            MainFieldProjectorTile mainTile = (MainFieldProjectorTile) w.getBlockEntity(mainProjector);
            if (mainTile == null)
                return;

            mainTile.setFieldInfo(fp);
        });
    }

    public static void handleFieldDeactivation(BlockPos center, FieldProjectionSize fieldSize) {
        BlockPos mainProjector = ProjectorHelper.getProjectorLocationForDirection(center, Direction.NORTH, fieldSize);

        World w = Minecraft.getInstance().level;
        MainFieldProjectorTile mainTile = (MainFieldProjectorTile) w.getBlockEntity(mainProjector);
        if (mainTile == null)
            return;

        mainTile.invalidateField();
    }

    public static void handlePlayMiniaturizationSound(BlockPos fieldPreviewPos) {
        Minecraft.getInstance().getSoundManager().queueTickingSound(new MiniaturizationSound(fieldPreviewPos));
    }
}
