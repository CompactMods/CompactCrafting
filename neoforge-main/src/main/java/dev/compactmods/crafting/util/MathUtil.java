package dev.compactmods.crafting.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MathUtil {

    public static Vector3d toVector3d(Vec3 position) {
        return new Vector3d(position.x, position.y, position.z);
    }

    public static Vector3d toVector3d(BlockPos position) {
        final var centerMoj = Vec3.atCenterOf(position);
        return new Vector3d(centerMoj.x, centerMoj.y, centerMoj.z);
    }

    public static double calculateFieldScale(double progress, double requiredTime) {
        double waveDensity = 0.3d;
        double h = 0.2d;
        double p = 1 - (progress / requiredTime);
        double l = 2 * Math.PI / 0.5d;
        double n = Math.floor(requiredTime / l) + 0.5d;

        // q(progress) = w(x)
        double q = 0.5 * Math.cos(waveDensity * ((l * n * progress) / requiredTime)) + 0.5;

        double scale = p - (h * p) + (h * q);
        return scale;
    }

    public static ChunkPos toChunkPosition(Vector3dc center) {
        return new ChunkPos(SectionPos.blockToSectionCoord(center.x()), SectionPos.blockToSectionCoord(center.z()));
    }

    public static BlockPos toBlockPosition(Vector3dc center) {
        return BlockPos.containing(center.x(), center.y(), center.z());
    }
}
