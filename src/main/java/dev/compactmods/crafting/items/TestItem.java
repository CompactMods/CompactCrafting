package dev.compactmods.crafting.items;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.ui.container.TestContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class TestItem extends Item {
    public TestItem(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        if(!level.isClientSide) {
            INamedContainerProvider p = new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new TranslationTextComponent(CompactCrafting.MOD_ID.concat(".gui.test"));
                }

                @Nullable
                @Override
                public Container createMenu(int cid, PlayerInventory playerInv, PlayerEntity player) {
                    return new TestContainer(cid, level, playerInv, player);
                }
            };

            NetworkHooks.openGui((ServerPlayerEntity) player, p);
        }

        return ActionResult.pass(player.getItemInHand(hand));
    }
}
