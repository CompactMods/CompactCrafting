package com.robotgryphon.compactcrafting.proxies.item;

import com.robotgryphon.compactcrafting.field.capability.CapabilityMiniaturizationField;
import com.robotgryphon.compactcrafting.projector.block.FieldProjectorBlock;
import com.robotgryphon.compactcrafting.projector.tile.FieldProjectorTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FieldProxyItem extends BlockItem {
    public FieldProxyItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> text, ITooltipFlag flags) {

        boolean isLinked = false;
        if(stack.hasTag()) {
            CompoundNBT field = stack.getOrCreateTagElement("field");
            if(field.contains("center")) {
                isLinked = true;

                BlockPos linkedCenter = NBTUtil.readBlockPos(field.getCompound("center"));
                text.add(new TranslationTextComponent("tooltip.compactcrafting.proxy_bound", linkedCenter)
                    .withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.AQUA));
            }
        }

        if(!isLinked) {
            text.add(new TranslationTextComponent("tooltip.compactcrafting.proxy_bind_hint")
                .withStyle(TextFormatting.DARK_GRAY));
        } else {
            text.add(new TranslationTextComponent("tooltip.compactcrafting.proxy_unbind_hint")
                    .withStyle(TextFormatting.DARK_GRAY));
        }

        text.add(new TranslationTextComponent("tooltip.compactcrafting.proxy_hint").withStyle(TextFormatting.DARK_GRAY));

        super.appendHoverText(stack, level, text, flags);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isDiscrete() && hand == Hand.MAIN_HAND) {
            // used in the air while sneaking
            player.displayClientMessage(new StringTextComponent("clearing field data"), true);

            // clear field position
            stack.removeTagKey("field");

            return ActionResult.success(stack);
        }

        return ActionResult.pass(stack);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World level = context.getLevel();
        PlayerEntity player = context.getPlayer();

        ItemStack stack = context.getItemInHand();

        // player is sneaking - see if they used the proxy on a projector
        if (player.isDiscrete()) {
            BlockPos usedAt = context.getClickedPos();
            BlockState usedState = level.getBlockState(usedAt);

            // if used on a projector while sneaking
            if (usedState.getBlock() instanceof FieldProjectorBlock) {
                player.displayClientMessage(new StringTextComponent("copying field position"), true);

                FieldProjectorTile tile = (FieldProjectorTile) level.getBlockEntity(usedAt);
                if (tile != null) {
                    tile.getCapability(CapabilityMiniaturizationField.MINIATURIZATION_FIELD)
                            .ifPresent(field -> {
                                BlockPos fieldCenter = field.getCenter();

                                // write field center
                                stack.getOrCreateTagElement("field")
                                        .put("center", NBTUtil.writeBlockPos(fieldCenter));
                            });
                }

                return ActionResultType.sidedSuccess(level.isClientSide);
            }
        }

        return super.useOn(context);
    }
}
