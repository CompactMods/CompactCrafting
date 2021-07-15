package com.robotgryphon.compactcrafting.compat.theoneprobe.providers;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import dev.compactmods.compactcrafting.api.field.IMiniaturizationField;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
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
                    .resolve()
                    .flatMap(IMiniaturizationField::getCurrentRecipe)
                    .ifPresent(recipe -> {
                        IProbeInfo group = info.horizontal(info.defaultLayoutStyle()
                            .alignment(ElementAlignment.ALIGN_CENTER));

                        group.text(new TranslationTextComponent(CompactCrafting.MOD_ID + ".top.current_recipe"));

                        group.item(recipe.getCatalyst());
                        group.icon(
                                new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-outputs.png"),
                                0, 0, 24, 19, info.defaultIconStyle().textureHeight(19)
                        .textureWidth(24)
                        .height(19).width(24));

                        for(ItemStack out : recipe.getOutputs()) {
                            group.item(out);
                        }
                    });

            if(mode == ProbeMode.EXTENDED) {
                tile.getFieldBounds().ifPresent(bounds -> {
                    info.text(new StringTextComponent("Field bounds: " + bounds));
                });
            }
        }
    }
}
