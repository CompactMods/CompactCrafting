package dev.compactmods.crafting.api.field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.crafting.api.projector.placement.FieldProjectorPlacements;
import dev.compactmods.crafting.api.projector.placement.ProjectorPlacement;
import dev.compactmods.crafting.api.util.CCExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record MiniaturizationFieldLocation(Vector3dc center, MiniaturizationFieldSize size) {

    public static final Codec<MiniaturizationFieldLocation> CODEC = RecordCodecBuilder.create(i -> i.group(
            CCExtraCodecs.VECTOR3DC.fieldOf("center").forGetter(MiniaturizationFieldLocation::center),
            MiniaturizationFieldSize.CODEC.fieldOf("size").forGetter(MiniaturizationFieldLocation::size)
    ).apply(i, MiniaturizationFieldLocation::new));

    public static final StreamCodec<FriendlyByteBuf, MiniaturizationFieldLocation> STREAM_CODEC = StreamCodec.composite(
            CCExtraCodecs.VECTOR3DC_STREAM, MiniaturizationFieldLocation::center,
            MiniaturizationFieldSize.STREAM_CODEC, MiniaturizationFieldLocation::size,
            MiniaturizationFieldLocation::new
    );

    public static MiniaturizationFieldLocation compute(MiniaturizationFieldSize size, ProjectorPlacement projector) {
        final var center = new Vector3d()
                .add(Vec3.atCenterOf(projector.position()).toVector3f())
                .add(projector.facing().step().mul(size.getProjectorDistance() + 1));

        return new MiniaturizationFieldLocation(center, size);
    }

    /// Tries to find a matching field size based on the largest level of an AABB.
    @NonNull
    public static Optional<MiniaturizationFieldLocation> compute(AABB bounds) {
        double biggestDim = Math.max(Math.max(bounds.getXsize(), bounds.getYsize()), bounds.getZsize());
        final var center = new Vector3d(bounds.getCenter().toVector3f());

        return MiniaturizationFieldSize.fromDimensions(biggestDim)
                .map(size -> new MiniaturizationFieldLocation(center, size));
    }

    /// Tries to find a matching field size based on the largest level of a bounding box.
    @NonNull
    public static Optional<MiniaturizationFieldLocation> compute(BoundingBox bounds) {
        int max1 = Math.max(bounds.getXSpan(), bounds.getYSpan());
        int biggest = Math.max(max1, bounds.getZSpan());
        final var center = new Vector3d(Vec3.atCenterOf(bounds.getCenter()).toVector3f());

        return MiniaturizationFieldSize.fromDimensions(biggest)
                .map(size -> new MiniaturizationFieldLocation(center, size));
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
