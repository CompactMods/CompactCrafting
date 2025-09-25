package dev.compactmods.crafting.proxies.item;

import java.util.List;
import dev.compactmods.crafting.core.CCDataComponents;
import dev.compactmods.crafting.data.CCAttachments;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldProxyItem extends BlockItem {
    public FieldProxyItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.@Nullable TooltipContext context, List<Component> text, TooltipFlag flags) {

        var fieldCenter = stack.get(CCDataComponents.FIELD_CENTER.get());
        boolean isLinked = fieldCenter != null;
        
        if(isLinked) {
            BlockPos linkedCenter = fieldCenter.center();
            text.add(Component.translatable("tooltip.compactcrafting.proxy_bound", linkedCenter.toString())
                .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.AQUA));
        }

        if(!isLinked) {
            text.add(Component.translatable("tooltip.compactcrafting.proxy_bind_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            text.add(Component.translatable("tooltip.compactcrafting.proxy_unbind_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        text.add(Component.translatable("tooltip.compactcrafting.proxy_hint").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, text, flags);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(player.isDiscrete() && hand == InteractionHand.MAIN_HAND) {
            player.displayClientMessage(Component.translatable("compactcrafting.unbinding_proxy"), true);

            stack.remove(CCDataComponents.FIELD_CENTER.get());

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

            if (usedState.getBlock() instanceof FieldProjectorBlock) {

                player.displayClientMessage(Component.translatable("compactcrafting.binding_proxy", usedAt.toString()), true);

                FieldProjectorEntity tile = (FieldProjectorEntity) level.getBlockEntity(usedAt);
                if (tile != null) {
                    var fields = level.getData(CCAttachments.ACTIVE_FIELDS);
                    
                    // Search through all fields to find one whose projectors include this position
                    fields.getFields()
                        .filter(field -> field.getProjectors().locations().contains(usedAt))
                        .findFirst()
                        .ifPresent(field -> {
                            BlockPos fieldCenter = field.getCenter();

                            stack.set(CCDataComponents.FIELD_CENTER.get(), new CCDataComponents.FieldCenter(fieldCenter));
                        });
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useOn(context);
    }
}