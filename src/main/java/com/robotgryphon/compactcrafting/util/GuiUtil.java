package com.robotgryphon.compactcrafting.util;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.inventory.MiniaturizationRecipeContainer;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class GuiUtil {
    private static final ResourceLocation USELESS_ID = new ResourceLocation(CompactCrafting.MOD_ID, "useless");
    private static final ITextComponent VIEW_RECIPE = new TranslationTextComponent("container.miniaturization.view");
    private static final ITextComponent COPY_RECIPE = new TranslationTextComponent("container.miniaturization.copy");

    private GuiUtil() {}

    public static void openMiniaturizationGui(ServerPlayerEntity player, MiniaturizationRecipe recipe, boolean modifiable) {
        SimpleNamedContainerProvider provider = new SimpleNamedContainerProvider((id, playerInv, p) -> new MiniaturizationRecipeContainer(id, playerInv, recipe, modifiable),
                modifiable ? COPY_RECIPE : VIEW_RECIPE);
        NetworkHooks.openGui(player, provider, buf -> {
            ResourceLocation id = recipe.getId();
            buf.writeResourceLocation(id == null ? USELESS_ID : id);
            Registration.MINIATURIZATION_SERIALIZER.get().toNetwork(buf, recipe);
            buf.writeBoolean(modifiable);
        });
    }
}
