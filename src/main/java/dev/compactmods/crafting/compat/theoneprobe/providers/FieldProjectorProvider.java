package dev.compactmods.crafting.compat.theoneprobe.providers;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.crafting.projector.block.FieldProjectorBlock;
import dev.compactmods.crafting.projector.tile.FieldProjectorTile;
import dev.compactmods.crafting.api.EnumCraftingState;
import dev.compactmods.crafting.api.recipe.IMiniaturizationRecipe;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class FieldProjectorProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return CompactCrafting.MOD_ID + "_projector";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World level, BlockState state, IProbeHitData hitData) {
        if (!(state.getBlock() instanceof FieldProjectorBlock))
            return;

        // add info from server
        if (FieldProjectorBlock.isActive(state)) {
            FieldProjectorTile tile = (FieldProjectorTile) level.getBlockEntity(hitData.getPos());
            if (tile == null)
                return;

            tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                    .ifPresent(field -> {
                        IMiniaturizationRecipe recipe = field.getCurrentRecipe().orElse(null);

                        final IProbeInfo recipeProgress = info.vertical(
                                info.defaultLayoutStyle()
                                        .alignment(ElementAlignment.ALIGN_CENTER)
                                        .spacing(1));

                        IProbeInfo group = recipeProgress.horizontal(info.defaultLayoutStyle()
                                .alignment(ElementAlignment.ALIGN_CENTER));

                        // group.text(new TranslationTextComponent(CompactCrafting.MOD_ID + ".top.current_recipe"));

                        if(recipe != null) {
                            int progress = field.getProgress();

                            group.item(recipe.getCatalyst());
                            group.icon(
                                    new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-outputs.png"),
                                    0, 0, 24, 19, info.defaultIconStyle().textureHeight(19)
                                            .textureWidth(24)
                                            .height(19).width(24));

                            for (ItemStack out : recipe.getOutputs()) {
                                group.item(out);
                            }

                            recipeProgress
                                    .progress(1, 1, info.defaultProgressStyle()
                                            .showText(false).borderColor(0).backgroundColor(0)
                                            .height(0).width(110).filledColor(0)
                                            .alternateFilledColor(0))

                                    .progress(progress, recipe.getCraftingTime(),
                                            info.defaultProgressStyle()
                                                    .height(5)
                                                    .width(100)
                                                    .showText(false)
                                                    .filledColor(0xFFCCCCCC)
                                                    .alternateFilledColor(0xFFCCCCCC)
                                                    .backgroundColor(Color.rgb(255, 250, 250, 50))
                                                    .borderColor(0x00000000))
                            ;

                            if (field.getCraftingState() == EnumCraftingState.MATCHED && progress == 0) {
                                recipeProgress
                                        .text(new TranslationTextComponent(CompactCrafting.MOD_ID + ".top.awaiting_catalyst"));

                            } else {
                                if(mode == ProbeMode.EXTENDED) {
                                    recipeProgress
                                            .text(new TranslationTextComponent(CompactCrafting.MOD_ID + ".top.progress", progress, recipe.getCraftingTime()));
                                }
                            }
                        }
                    });
        }
    }
}
