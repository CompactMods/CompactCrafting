package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.field.location.MiniaturizationFieldLocation;
import dev.compactmods.crafting.field.MiniaturizationFields;
import dev.compactmods.crafting.field.impl.MiniaturizationField;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ClientPacketHandler {

    public static void handleFieldActivation(MiniaturizationFieldLocation location) {
        Minecraft mc = Minecraft.getInstance();
        mc.submitAsync(() -> {
            ClientLevel cw = mc.level;
            if (cw == null)
                return;

            final var newField = new MiniaturizationField(cw, location);
            cw.getData(MiniaturizationFields.ACTIVE_FIELDS).registerField(newField);
        });
    }

    public static void handleFieldDeactivation(MiniaturizationFieldLocation location) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        mc.submitAsync(() -> {
            mc.level.getExistingData(MiniaturizationFields.ACTIVE_FIELDS).ifPresent(fields -> {
                fields.get(location).ifPresent(field -> {
                    final var projectors = field.getProjectors();
                    projectors.disableAll(mc.level);

                    fields.unregisterField(location.center());
                });
            });
        });
    }

    public static void clearFieldRecipe(MiniaturizationFieldLocation location) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        // TODO
//        mc.level.getData(MiniaturizationFields.ACTIVE_FIELDS)
//                .get(location.center())
//                .ifPresent(IMiniaturizationField::clearRecipe);
    }

    public static void changeFieldRecipe(MiniaturizationFieldLocation location, RecipeHolder<MiniaturizationRecipe> recipe) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;

        // TODO
//        mc.level.getData(MiniaturizationFields.ACTIVE_FIELDS)
//                .get(location.center())
//                .filter(field -> field instanceof IMutableMiniaturizationField)
//                .map(IMutableMiniaturizationField.class::cast)
//                .ifPresent(field -> {
//                    field.setRecipe(recipe);
//                    field.spawnParticlesAtProjectors(MiniaturizationField.RECIPE_MATCHED_PARTICLE_OPTS);
//                });

    }
}
