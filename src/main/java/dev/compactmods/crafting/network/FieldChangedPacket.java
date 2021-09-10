package dev.compactmods.crafting.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import net.minecraft.util.math.BlockPos;

public class FieldChangedPacket {

    protected MiniaturizationFieldSize fieldSize;
    protected BlockPos fieldCenter;
    protected BlockPos[] projectorLocations;

    protected static final Codec<FieldChangedPacket> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.xmap(MiniaturizationFieldSize::valueOf, Enum::name)
                    .fieldOf("size").forGetter(x -> x.fieldSize),

            BlockPos.CODEC.fieldOf("center").forGetter(x -> x.fieldCenter)

    ).apply(i, FieldChangedPacket::new));

    protected FieldChangedPacket(MiniaturizationFieldSize fieldSize, BlockPos center) {
        this.fieldSize = fieldSize;
        this.fieldCenter = center;
        this.projectorLocations = fieldSize.getProjectorLocations(center).toArray(BlockPos[]::new);
    }
}
