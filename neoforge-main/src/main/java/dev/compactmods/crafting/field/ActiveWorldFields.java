package dev.compactmods.crafting.field;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.IActiveWorldFields;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.data.NbtListCollector;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.network.NetworkHandler;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import dev.compactmods.crafting.projector.ProjectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class ActiveWorldFields implements IActiveWorldFields, INBTSerializable<ListTag> {

    private Level level;

    /**
     * Holds a set of miniaturization fields that are active, referenced by their center point.
     */
    private final HashMap<BlockPos, IMiniaturizationField> fields;
    private final HashMap<BlockPos, LazyOptional<IMiniaturizationField>> laziness;

    public ActiveWorldFields() {
        this.fields = new HashMap<>();
        this.laziness = new HashMap<>();
    }

    public ActiveWorldFields(Level level) {
        this();
        this.level = level;
    }


    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public Stream<IMiniaturizationField> getFields() {
        return fields.values().stream();
    }

    public void tickFields() {
        Set<IMiniaturizationField> loaded = fields.values().stream()
                .filter(IMiniaturizationField::isLoaded)
                .collect(Collectors.toSet());

        if (loaded.isEmpty())
            return;

        CompactCrafting.LOGGER.trace("Loaded count ({}): {}", level.dimension().location(), loaded.size());
        loaded.forEach(IMiniaturizationField::tick);
    }

    @Override
    public void addFieldInstance(IMiniaturizationField field) {
        field.setLevel(level);

        BlockPos center = field.getCenter();
        fields.put(center, field);

        LazyOptional<IMiniaturizationField> lazy = LazyOptional.of(() -> field);
        laziness.put(center, lazy);
        field.setRef(lazy);

        lazy.addListener(lo -> {
            lo.ifPresent(this::unregisterField);
        });
    }

    public IMiniaturizationField registerField(IMiniaturizationField field) {
        final Optional<BlockPos> anyMissing = ProjectorHelper
                .getMissingProjectors(level, field.getFieldSize(), field.getCenter())
                .findFirst();

        if (anyMissing.isPresent()) {
            CompactCrafting.LOGGER.warn("Trying to register an active field with missing projector at {}; real state: {}", anyMissing.get(), level.getBlockState(anyMissing.get()));
            return field;
        }

        addFieldInstance(field);
        field.getProjectorPositions().forEach(pos -> {
            BlockState stateAt = level.getBlockState(pos);
            if (!(stateAt.getBlock() instanceof FieldProjectorBlock))
                return;

            if (stateAt.hasBlockEntity()) {
                BlockEntity tileAt = level.getBlockEntity(pos);
                if (tileAt instanceof FieldProjectorEntity) {
                    ((FieldProjectorEntity) tileAt).setFieldRef(field.getRef());
                }
            }
        });

        return field;
    }

    public void unregisterField(BlockPos center) {
        if (fields.containsKey(center)) {
            IMiniaturizationField removedField = fields.remove(center);
            final LazyOptional<IMiniaturizationField> removed = laziness.remove(center);
            removed.invalidate();

            if (!level.isClientSide && removedField != null) {
                // Send activation packet to clients
                NetworkHandler.MAIN_CHANNEL.send(
                        PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(removedField.getCenter())),
                        new FieldDeactivatedPacket(removedField.getFieldSize(), removedField.getCenter()));
            }
        }
    }

    public void unregisterField(IMiniaturizationField field) {
        BlockPos center = field.getCenter();
        unregisterField(center);
    }

    public LazyOptional<IMiniaturizationField> getLazy(BlockPos center) {
        return laziness.getOrDefault(center, LazyOptional.empty());
    }

    @Override
    public Optional<IMiniaturizationField> get(BlockPos center) {
        return Optional.ofNullable(fields.getOrDefault(center, null));
    }

    @Override
    public boolean hasActiveField(BlockPos center) {
        return fields.containsKey(center);
    }

    @Override
    public Stream<IMiniaturizationField> getFields(ChunkPos chunk) {
        return fields.entrySet()
                .stream()
                .filter(p -> new ChunkPos(p.getKey()).equals(chunk))
                .map(Map.Entry::getValue);
    }

    @Override
    public ResourceKey<Level> getLevel() {
        return level.dimension();
    }

    @Override
    public ListTag serializeNBT() {
        return getFields()
                .map(IMiniaturizationField::serverData)
                .collect(NbtListCollector.toNbtList());
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        nbt.forEach(item -> {
            if (item instanceof CompoundTag ct) {
                MiniaturizationField field = new MiniaturizationField(ct);
                addFieldInstance(field);
            }
        });
    }
}
