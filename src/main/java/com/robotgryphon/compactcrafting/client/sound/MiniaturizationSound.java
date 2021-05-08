package com.robotgryphon.compactcrafting.client.sound;

import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class MiniaturizationSound extends TickableSound {
    private final BlockPos fieldPreviewPos;
    private final Minecraft minecraft = Minecraft.getInstance();
    private int tickCount = 0;

    public MiniaturizationSound(BlockPos fieldPreviewPos) {
        super(Registration.MINIATURIZATION_CRAFTING_SOUND.get(), SoundCategory.BLOCKS);
        this.fieldPreviewPos = fieldPreviewPos;
        this.looping = true;
        this.volume = 0.4F;
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (minecraft.level == null) {
            this.stop();
            return;
        }
        tickCount++;
        TileEntity fieldPreviewTile = minecraft.level.getBlockEntity(fieldPreviewPos);
        // Give at least 5 ticks so the player can at least hear it and so that the tile entity exists on the client
        if (tickCount > 5 && (minecraft.level == null || fieldPreviewTile == null || fieldPreviewTile.isRemoved()))
            this.stop();
    }
}
