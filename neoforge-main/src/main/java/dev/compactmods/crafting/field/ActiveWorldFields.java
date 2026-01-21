package dev.compactmods.crafting.field;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActiveWorldFields {

    private final Level level;

    /**
     * Holds a set of miniaturization fields that are active, referenced by their center point.
     */
    private final HashMap<Vector3dc, IMiniaturizationField> fields = new HashMap<>();

    private ActiveWorldFields(Level level) {
        this.level = level;
    }

    public static ActiveWorldFields create(@NotNull Level level) {
        return new ActiveWorldFields(level);
    }

    public Stream<IMiniaturizationField> getFields() {
        return fields.values().stream();
    }

    public void tickFields() {
        Set<IMiniaturizationField> loaded = fields.values().stream()
                .filter(IMiniaturizationField::isAreaLoaded)
                .collect(Collectors.toSet());

        if (loaded.isEmpty())
            return;

        CompactCrafting.LOGGER.trace("Loaded count ({}): {}", level.dimension().identifier(), loaded.size());
        loaded.forEach(IMiniaturizationField::tick);
    }

    public void addFieldInstance(IMiniaturizationField field) {
        fields.put(field.location().center(), field);

        // TODO: Attachment for projectors invalidation
//        LazyOptional<IMiniaturizationField> lazy = LazyOptional.of(() -> projectors);
//        laziness.put(center, lazy);
//        projectors.setRef(lazy);
//
//        lazy.addListener(lo -> {
//            lo.ifPresent(this::unregisterField);
//        });
    }

    public void registerField(IMiniaturizationField field) {
        addFieldInstance(field);

        // FIXME - Set projector back-references to projectors
        //        projectors.getProjectors().locations().forEach(pos -> {
//            BlockState stateAt = level.getBlockState(pos);
//            if (!(stateAt.getBlock() instanceof FieldProjectorBlock))
//                return;
//
//            if (stateAt.hasBlockEntity()) {
//                BlockEntity tileAt = level.getBlockEntity(pos);
//                if (tileAt instanceof FieldProjectorEntity) {
//                    // ((FieldProjectorEntity) tileAt).setFieldRef(projectors.getRef());
//                }
//            }
//        });
    }

    public void unregisterField(Vector3dc center) {
        if (fields.containsKey(center)) {
            var removedField = fields.remove(center);
//            final LazyOptional<IMiniaturizationField> removed = laziness.remove(center);
//            removed.invalidate();

            if (!level.isClientSide() && removedField != null && level instanceof ServerLevel sl) {
                // Send deactivation packet to clients
                PacketDistributor.sendToPlayersTrackingChunk(sl,
                        MathUtil.toChunkPosition(removedField.location().center()),
                        new FieldDeactivatedPacket(removedField.location()));
            }
        }
    }

    public void unregisterField(IMiniaturizationField field) {
        unregisterField(field.location().center());
    }

    public Optional<IMiniaturizationField> get(Vector3dc center) {
        return Optional.ofNullable(fields.getOrDefault(center, null));
    }

    public boolean hasActiveField(Vector3dc center) {
        return fields.containsKey(center);
    }

    public Stream<IMiniaturizationField> getFields(ChunkPos chunk) {
        return fields.entrySet()
                .stream()
                .filter(p -> MathUtil.toChunkPosition(p.getKey()).equals(chunk))
                .map(Map.Entry::getValue);
    }

    public ResourceKey<Level> getLevel() {
        return level.dimension();
    }
}
