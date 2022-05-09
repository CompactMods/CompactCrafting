package dev.compactmods.crafting.client;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.FieldSize;
import dev.compactmods.crafting.api.field.MiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationRecipeHolder;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.field.client.ClientMiniaturizationField;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class ClientPacketHandler {

    public static void fieldActivatedNearby(FieldSize size, BlockPos center, CompoundTag fieldClientData) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            if (cw == null)
                return;

            ClientMiniaturizationField field = new ClientMiniaturizationField(cw, size, center, fieldClientData);
            mc.level.getCapability(CCCapabilities.FIELDS)
                    .ifPresent(fields -> fields.registerField(field));
        });
    }

    public static void handleFieldDeactivation(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            cw.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
                fields.get(center).map(MiniaturizationField::getProjectorPositions)
                        .orElse(Stream.empty())
                        .forEach(proj -> FieldProjectorBlock.deactivateProjector(cw, proj));

                fields.unregisterField(center);
            });
        });
    }

    public static void fieldBeganWatching(FieldSize size, BlockPos center, CompoundTag fieldData) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;
        ClientMiniaturizationField field = new ClientMiniaturizationField(mc.level, size, center, fieldData);

        mc.level.getCapability(CCCapabilities.FIELDS)
                .ifPresent(fields -> {
                    fields.setLevel(mc.level);

                    CompactCrafting.LOGGER.debug("Registering field on client");
                    final MiniaturizationField fieldRegistered = fields.registerField(field);

                    CompactCrafting.LOGGER.debug("Setting field references");
                    field.getProjectorPositions()
                            .map(mc.level::getBlockEntity)
                            .map(tile -> (FieldProjectorEntity) tile)
                            .filter(Objects::nonNull)
                            .forEach(tile -> {
                                tile.setFieldRef(fieldRegistered.getRef());
                            });
                });
    }

    public static void removeField(BlockPos fieldCenter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getCapability(CCCapabilities.FIELDS)
                .ifPresent(fields -> fields.unregisterField(fieldCenter));
    }

    public static void handleRecipeChanged(BlockPos center, @Nullable ResourceLocation recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getCapability(CCCapabilities.FIELDS)
                .lazyMap(af -> af.get(center))
                .ifPresent(field -> field.ifPresent(f -> {
                    if(f instanceof MiniaturizationRecipeHolder mf)
                        mf.setRecipe(recipe);
                }));
    }
}
