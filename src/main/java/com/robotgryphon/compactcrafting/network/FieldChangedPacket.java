package com.robotgryphon.compactcrafting.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.compactcrafting.api.field.FieldProjectionSize;
import net.minecraft.util.math.BlockPos;

public class FieldChangedPacket {

    protected FieldProjectionSize fieldSize;
    protected BlockPos fieldCenter;
    protected BlockPos[] projectorLocations;

    protected static final Codec<FieldChangedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(FieldProjectionSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.fieldSize),

            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.fieldCenter)

    ).apply(i, FieldChangedPacket::new));

    protected FieldChangedPacket(FieldProjectionSize fieldSize, BlockPos center) {
        this.fieldSize = fieldSize;
        this.fieldCenter = center;
        this.projectorLocations = fieldSize.getProjectorLocations(center).toArray(BlockPos[]::new);
    }
}
