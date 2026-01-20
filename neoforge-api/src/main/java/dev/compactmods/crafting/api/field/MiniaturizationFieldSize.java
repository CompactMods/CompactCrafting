package dev.compactmods.crafting.api.field;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public enum MiniaturizationFieldSize implements StringRepresentable {
    /**
     * 3x3x3 crafting field Size
     */
    SMALL("small", 1, 4),

    /**
     * 5x5x5 crafting field Size
     */
    MEDIUM("medium", 2, 6),

    /**
     * 7x7x7 crafting field Size
     */
    LARGE("large", 3, 8),

    /**
     * 9x9x9 crafting field size.
     */
    ABSURD("absurd", 4, 10);

    private final int radius;

    /**
     * Number of blocks between two projectors.
     */
    private final int projectorDistance;

    private final String name;

    public static final Codec<MiniaturizationFieldSize> CODEC =
            Codec.STRING.xmap(MiniaturizationFieldSize::valueOf, MiniaturizationFieldSize::name);

    public static final StreamCodec<FriendlyByteBuf, MiniaturizationFieldSize> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(MiniaturizationFieldSize::valueOf, MiniaturizationFieldSize::name)
                    .cast();

    MiniaturizationFieldSize(String name, int radius, int distance) {
        this.radius = radius;
        this.projectorDistance = distance;
        this.name = name;
    }

    public static boolean canFitDimensions(int dims) {
        return dims >= 1 && dims <= ABSURD.getDimensions();
    }

    /**
     * Gets the distance between the center of a field and a projector. (exclusive)
     *
     * @return
     */
    public int getProjectorDistance() {
        return this.projectorDistance;
    }

    public int getDimensions() {
        return (this.radius * 2) + 1;
    }

    public int getRadius() {
        return this.radius;
    }

    public String getName() {
        return this.name;
    }

    public static MiniaturizationFieldSize maximum() {
        return ABSURD;
    }

    public Vector3dc getOriginCenter() {
        final var centerBlock = BlockPos.ZERO.offset(radius, radius, radius);
        return new Vector3d(Vec3.atCenterOf(centerBlock).toVector3f());
    }

    public AABB toAABB(Vector3dc center) {
        return new AABB(BlockPos.containing(center.x(), center.y(), center.z())).inflate(radius);
    }

    @Nonnull
    public static Optional<MiniaturizationFieldSize> fromDimensions(double size) {
        // Mth.frac - checks if the decimal value was not an integer
        if(Mth.frac(size) > 0)
            return Optional.empty();

        // smaller than small, larger than max size, or not an odd size
        if(size < SMALL.getDimensions() || size > maximum().getDimensions() || size % 2 == 0)
            return Optional.empty();

        return Arrays.stream(values())
                .filter(s -> s.getDimensions() == size)
                .findFirst();
    }

    @Override
    public @NonNull String getSerializedName() {
        return name;
    }
}
