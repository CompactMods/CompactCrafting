package com.robotgryphon.compactcrafting.client.sound;

import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class MiniaturizationSound extends TickableSound {
    private final BlockPos fieldPreviewPos;

    public MiniaturizationSound(BlockPos fieldPreviewPos) {
        super(Registration.MINIATURIZATION_CRAFTING_SOUND.get(), SoundCategory.BLOCKS);
        this.fieldPreviewPos = fieldPreviewPos;
        this.looping = true;
        this.volume = 0.4F;
        this.delay = 0;
    }

    @Override
    public void tick() {
        ClientWorld level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(this.fieldPreviewPos) == null)
            this.stop();
    }
}
