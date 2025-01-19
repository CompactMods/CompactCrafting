package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.field.IMiniaturizationField;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.field.IMutableMiniaturizationField;
import dev.compactmods.crafting.field.MiniaturizationField;
import dev.compactmods.crafting.network.FieldActivatedPacket;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.stream.Stream;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(IMiniaturizationField<MiniaturizationRecipe> field, CompoundTag fieldClientData) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            if (cw == null)
                return;

            // FIXME field.loadClientData(fieldClientData);

            mc.level.getData(CCAttachments.ACTIVE_FIELDS).registerField(field);
        });
    }

    public static void handleFieldDeactivation(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        mc.submitAsync(() -> {
            mc.level.getExistingData(CCAttachments.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.get(center).ifPresent(field -> {
                    final var projectors = field.getProjectors();
                    projectors.disableAll();
                });

                fields.unregisterField(center);
            });
        });
    }

//    public static void handleFieldData(CompoundTag fieldData) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.level == null)
//            return;
//
//        MiniaturizationField field = new MiniaturizationField();
//        field.setLevel(mc.level);
//        // FIXME field.loadClientData(fieldData);
//
////        mc.level.getCapability(CCCapabilities.FIELDS)
////                .ifPresent(fields -> {
////                    fields.setLevel(mc.level);
////                    CompactCrafting.LOGGER.debug("Registering field on client");
////                    final IMiniaturizationField fieldRegistered = fields.registerField(field);
////
////                    CompactCrafting.LOGGER.debug("Setting field references");
////
////                    field.getProjectorPositions()
////                            .map(mc.level::getBlockEntity)
////                            .map(tile -> (FieldProjectorEntity) tile)
////                            .filter(Objects::nonNull)
////                            .forEach(tile -> {
////                                final BlockState state = tile.getBlockState();
////                                tile.setFieldRef(fieldRegistered.getRef());
////                            });
////                });
//    }

    public static void removeField(BlockPos fieldCenter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS).unregisterField(fieldCenter);
    }

    public static void clearFieldRecipe(BlockPos center) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS)
                .get(center)
                .filter(field -> field instanceof IMutableMiniaturizationField)
                .map(IMutableMiniaturizationField.class::cast)
                .ifPresent(IMutableMiniaturizationField::clearRecipe);
    }

    public static void changeFieldRecipe(BlockPos center, RecipeHolder<MiniaturizationRecipe> recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        mc.level.getData(CCAttachments.ACTIVE_FIELDS)
                .get(center)
                .filter(field -> field instanceof IMutableMiniaturizationField)
                .map(IMutableMiniaturizationField.class::cast)
                .ifPresent(field -> {
                    field.setRecipe(recipe);
                    field.spawnParticlesAtProjectors(MiniaturizationField.RECIPE_MATCHED_PARTICLE_OPTS);
                });

    }

    public static FieldActivatedPacket createFieldActivationPacket(MiniaturizationFieldSize fieldSize, BlockPos center, CompoundTag clientData) {
        var field = new MiniaturizationField(Minecraft.getInstance().level, fieldSize, center);
        return new FieldActivatedPacket(field, clientData);
    }
}
