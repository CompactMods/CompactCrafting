package dev.compactmods.crafting.field;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.ITickingMiniaturizationField;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.projector.ProjectorHelper;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
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
    private final HashMap<BlockPos, IMiniaturizationField<MiniaturizationRecipe>> fields = new HashMap<>();

    private ActiveWorldFields(Level level) {
        this.level = level;
    }

    public static ActiveWorldFields create(@NotNull Level level) {
        return new ActiveWorldFields(level);
    }

    public Stream<IMiniaturizationField<MiniaturizationRecipe>> getFields() {
        return fields.values().stream();
    }

    public void tickFields() {
        Set<ITickingMiniaturizationField> loaded = fields.values().stream()
                .filter(IMiniaturizationField::isAreaLoaded)
                .filter(field -> field instanceof ITickingMiniaturizationField)
                .map(ITickingMiniaturizationField.class::cast)
                .collect(Collectors.toSet());

        if (loaded.isEmpty())
            return;

        CompactCrafting.LOGGER.trace("Loaded count ({}): {}", level.dimension().identifier(), loaded.size());
        loaded.forEach(ITickingMiniaturizationField::tick);
    }

    public void addFieldInstance(IMiniaturizationField<MiniaturizationRecipe> field) {
        BlockPos center = field.getCenter();
        fields.put(center, field);

        // TODO: Attachment for field invalidation
//        LazyOptional<IMiniaturizationField> lazy = LazyOptional.of(() -> field);
//        laziness.put(center, lazy);
//        field.setRef(lazy);
//
//        lazy.addListener(lo -> {
//            lo.ifPresent(this::unregisterField);
//        });
    }

    public IMiniaturizationField<MiniaturizationRecipe> registerField(IMiniaturizationField<MiniaturizationRecipe> field) {
        final Optional<BlockPos> anyMissing = ProjectorHelper
                .getMissingProjectors(level, field.getFieldSize(), field.getCenter())
                .findFirst();

        if (anyMissing.isPresent()) {
            CompactCrafting.LOGGER.warn("Trying to register an active field with missing projector at {}; real state: {}", anyMissing.get(), level.getBlockState(anyMissing.get()));
            return field;
        }

        addFieldInstance(field);

        // FIXME - Set projector back-references to field
        //        field.getProjectors().locations().forEach(pos -> {
//            BlockState stateAt = level.getBlockState(pos);
//            if (!(stateAt.getBlock() instanceof FieldProjectorBlock))
//                return;
//
//            if (stateAt.hasBlockEntity()) {
//                BlockEntity tileAt = level.getBlockEntity(pos);
//                if (tileAt instanceof FieldProjectorEntity) {
//                    // ((FieldProjectorEntity) tileAt).setFieldRef(field.getRef());
//                }
//            }
//        });

        return field;
    }

    public void unregisterField(BlockPos center) {
        if (fields.containsKey(center)) {
            var removedField = fields.remove(center);
//            final LazyOptional<IMiniaturizationField> removed = laziness.remove(center);
//            removed.invalidate();

            if (!level.isClientSide() && removedField != null && level instanceof ServerLevel sl) {
                // Send deactivation packet to clients
                PacketDistributor.sendToPlayersTrackingChunk(sl, ChunkPos.containing(removedField.getCenter()),
                        new FieldDeactivatedPacket(removedField.getFieldSize(), removedField.getCenter(), List.copyOf(removedField.getProjectors().locations())));
            }
        }
    }

    public void unregisterField(IMiniaturizationField<MiniaturizationRecipe> field) {
        BlockPos center = field.getCenter();
        unregisterField(center);
    }

    public Optional<IMiniaturizationField<MiniaturizationRecipe>> get(BlockPos center) {
        return Optional.ofNullable(fields.getOrDefault(center, null));
    }

    public boolean hasActiveField(BlockPos center) {
        return fields.containsKey(center);
    }

    public Stream<IMiniaturizationField<MiniaturizationRecipe>> getFields(ChunkPos chunk) {
        return fields.entrySet()
                .stream()
                .filter(p -> ChunkPos.containing(p.getKey()).equals(chunk))
                .map(Map.Entry::getValue);
    }

    public ResourceKey<Level> getLevel() {
        return level.dimension();
    }



//    public ListTag serializeNBT() {
//        return getFields()
//                .map(IMiniaturizationField::serverData)
//                .collect(NbtListCollector.toNbtList());
//    }
//


//    public void deserializeNBT(ListTag nbt) {
//        nbt.forEach(item -> {
//            if (item instanceof CompoundTag ct) {
//                MiniaturizationField field = new MiniaturizationField(ct);
//                addFieldInstance(field);
//            }
//        });
//    }
}
