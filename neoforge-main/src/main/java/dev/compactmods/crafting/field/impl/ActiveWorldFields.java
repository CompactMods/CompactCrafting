package dev.compactmods.crafting.field.impl;

import com.mojang.serialization.Codec;
import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.data.CodecHolder;
import dev.compactmods.crafting.field.ServerFieldDataManager;
import dev.compactmods.crafting.network.FieldDeactivatedPacket;
import dev.compactmods.crafting.util.MathUtil;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3dc;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ActiveWorldFields implements CodecHolder<ActiveWorldFields> {

    private final Level level;

    /**
     * Holds a set of miniaturization fields that are active, referenced by their center point.
     */
    private final HashMap<Vector3dc, MiniaturizationField> fields = new HashMap<>();

    public ActiveWorldFields(Level level) {
        this.level = level;
    }

    public void save(ServerFieldDataManager fieldData) {
        for(var field : fields.values())
            fieldData.setData(field.location(), field.craftingState());
    }

    public void tickFields() {
        Set<IMiniaturizationField> loaded = fields.values().stream()
                .filter(IMiniaturizationField::isAreaLoaded)
                .collect(Collectors.toSet());

        if (loaded.isEmpty())
            return;

//        CompactCrafting.LOGGER.trace("Loaded count ({}): {}", level.dimension().identifier(), loaded.size());
        loaded.forEach(IMiniaturizationField::tick);
    }

    public void registerField(MiniaturizationField field) {
        fields.put(field.location().center(), field);

        if(!level.isClientSide()) {
            final var center = field.location().centerBlock();
            final var section = SectionPos.of(center);
            final var registry = level.getChunkAt(center)
                    .getListenerRegistry(section.y());

            registry.register(field.changeListener);
        }
    }

    public void unregisterField(Vector3dc center) {
        if (fields.containsKey(center)) {
            var removedField = fields.remove(center);

            if (!level.isClientSide() && removedField != null && level instanceof ServerLevel sl) {
                // Send deactivation packet to clients
                PacketDistributor.sendToPlayersTrackingChunk(sl,
                        MathUtil.toChunkPosition(removedField.location().center()),
                        new FieldDeactivatedPacket(removedField.location()));

                final var centerBlock = MathUtil.toBlockPosition(center);
                final var section = SectionPos.of(centerBlock);
                final var registry = level.getChunkAt(centerBlock)
                        .getListenerRegistry(section.y());

                registry.register(removedField.changeListener);
            }
        }
    }

    public Optional<IMiniaturizationField> get(MiniaturizationFieldLocation location) {
        return Optional.ofNullable(fields.getOrDefault(location.center(), null));
    }

    public boolean hasActiveField(MiniaturizationFieldLocation location) {
        return fields.containsKey(location.center());
    }

    @Override
    public Codec<ActiveWorldFields> codec() {
        return null;
    }
}
