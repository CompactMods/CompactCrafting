package dev.compactmods.crafting.api.field.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import dev.compactmods.crafting.api.util.CCExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record MiniaturizationFieldLocation(ResourceKey<Level> dimension, Vector3dc center, MiniaturizationFieldSize size) {

    public static final Codec<MiniaturizationFieldLocation> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(MiniaturizationFieldLocation::dimension),
            CCExtraCodecs.VECTOR3DC.fieldOf("center").forGetter(MiniaturizationFieldLocation::center),
            MiniaturizationFieldSize.CODEC.fieldOf("size").forGetter(MiniaturizationFieldLocation::size)
    ).apply(i, MiniaturizationFieldLocation::new));

    public static final StreamCodec<FriendlyByteBuf, MiniaturizationFieldLocation> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), MiniaturizationFieldLocation::dimension,
            CCExtraCodecs.VECTOR3DC_STREAM, MiniaturizationFieldLocation::center,
            MiniaturizationFieldSize.STREAM_CODEC, MiniaturizationFieldLocation::size,
            MiniaturizationFieldLocation::new
    );

    public static MiniaturizationFieldLocation compute(ResourceKey<Level> level, MiniaturizationFieldSize size, ProjectorPlacement projector) {
        final var center = new Vector3d()
                .add(Vec3.atCenterOf(projector.position().pos()).toVector3f())
                .add(projector.facing().step().mul(size.getProjectorDistance() + 1));

        return new MiniaturizationFieldLocation(level, center, size);
    }

    public FieldProjectorPlacements projectors() {
        return FieldProjectorPlacements.compute(this);
    }

    public BlockPos centerBlock() {
        return BlockPos.containing(center.x(), center.y(), center.z()).immutable();
    }

    public AABB bounds() {
        return size.toAABB(center);
    }

    public BoundingBox box() {
        return new BoundingBox(centerBlock()).inflatedBy(size.getRadius());
    }

    public Vec3i boundsVec3i() {
        final var bounds = box();
        return new Vec3i(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
    }
}
