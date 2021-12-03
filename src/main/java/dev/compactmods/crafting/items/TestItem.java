package dev.compactmods.crafting.items;

import javax.annotation.Nullable;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.ui.container.TestContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class TestItem extends Item {
    public TestItem(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide) {
            MenuProvider p = new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return new TranslatableComponent(CompactCrafting.MOD_ID.concat(".gui.test"));
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int cid, Inventory playerInv, Player player) {
                    return new TestContainer(cid, level, playerInv, player);
                }
            };

            NetworkHooks.openGui((ServerPlayer) player, p);
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
