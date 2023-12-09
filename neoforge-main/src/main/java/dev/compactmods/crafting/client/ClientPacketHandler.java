package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.field.MiniaturizationField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(IMiniaturizationField field, CompoundTag fieldClientData) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            if (cw == null)
                return;

            field.setLevel(cw);
            field.loadClientData(fieldClientData);

//            mc.level.getCapability(CCCapabilities.FIELDS)
//                    .ifPresent(fields -> fields.registerField(field));
        });
    }

    public static void handleFieldDeactivation(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
//            cw.getCapability(CCCapabilities.FIELDS).ifPresent(fields -> {
//                fields.get(center).map(IMiniaturizationField::getProjectorPositions)
//                        .orElse(Stream.empty())
//                        .forEach(proj -> FieldProjectorBlock.deactivateProjector(cw, proj));
//
//                fields.unregisterField(center);
//            });
        });
    }

    public static void handleFieldData(CompoundTag fieldData) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        MiniaturizationField field = new MiniaturizationField();
        field.setLevel(mc.level);
        field.loadClientData(fieldData);

//        mc.level.getCapability(CCCapabilities.FIELDS)
//                .ifPresent(fields -> {
//                    fields.setLevel(mc.level);
//                    CompactCrafting.LOGGER.debug("Registering field on client");
//                    final IMiniaturizationField fieldRegistered = fields.registerField(field);
//
//                    CompactCrafting.LOGGER.debug("Setting field references");
//
//                    field.getProjectorPositions()
//                            .map(mc.level::getBlockEntity)
//                            .map(tile -> (FieldProjectorEntity) tile)
//                            .filter(Objects::nonNull)
//                            .forEach(tile -> {
//                                final BlockState state = tile.getBlockState();
//                                tile.setFieldRef(fieldRegistered.getRef());
//                            });
//                });
    }

    public static void removeField(BlockPos fieldCenter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

//        mc.level.getCapability(CCCapabilities.FIELDS)
//                .ifPresent(fields -> fields.unregisterField(fieldCenter));
    }

    public static void handleRecipeChanged(BlockPos center, @Nullable ResourceLocation recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

//        mc.level.getCapability(CCCapabilities.FIELDS)
//                .lazyMap(af -> af.get(center))
//                .ifPresent(field -> field.ifPresent(f -> f.setRecipe(recipe)));
    }
}
