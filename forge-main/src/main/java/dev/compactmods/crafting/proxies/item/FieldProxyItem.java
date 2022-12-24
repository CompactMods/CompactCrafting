package dev.compactmods.crafting.proxies.item;

import javax.annotation.Nullable;
import java.util.List;
import dev.compactmods.crafting.core.CCCapabilities;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FieldProxyItem extends BlockItem {
    public FieldProxyItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> text, TooltipFlag flags) {

        boolean isLinked = false;
        if(stack.hasTag()) {
            CompoundTag field = stack.getOrCreateTagElement("field");
            if(field.contains("center")) {
                isLinked = true;

                BlockPos linkedCenter = NbtUtils.readBlockPos(field.getCompound("center"));
                text.add(new TranslatableComponent("tooltip.compactcrafting.proxy_bound", linkedCenter)
                    .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.AQUA));
            }
        }

        if(!isLinked) {
            text.add(new TranslatableComponent("tooltip.compactcrafting.proxy_bind_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            text.add(new TranslatableComponent("tooltip.compactcrafting.proxy_unbind_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        text.add(new TranslatableComponent("tooltip.compactcrafting.proxy_hint").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, level, text, flags);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isDiscrete() && hand == InteractionHand.MAIN_HAND) {
            // used in the air while sneaking
            player.displayClientMessage(new TextComponent("clearing field data"), true);

            // clear field position
            stack.removeTagKey("field");

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        ItemStack stack = context.getItemInHand();

        // player is sneaking - see if they used the proxy on a projector
        if (player.isDiscrete()) {
            BlockPos usedAt = context.getClickedPos();
            BlockState usedState = level.getBlockState(usedAt);

            // if used on a projector while sneaking
            if (usedState.getBlock() instanceof FieldProjectorBlock) {
                player.displayClientMessage(new TextComponent("copying field position"), true);

                FieldProjectorEntity tile = (FieldProjectorEntity) level.getBlockEntity(usedAt);
                if (tile != null) {
                    tile.getCapability(CCCapabilities.MINIATURIZATION_FIELD)
                            .ifPresent(field -> {
                                BlockPos fieldCenter = field.getCenter();

                                // write field center
                                stack.getOrCreateTagElement("field")
                                        .put("center", NbtUtils.writeBlockPos(fieldCenter));
                            });
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useOn(context);
    }
}
