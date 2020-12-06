package com.robotgryphon.compactcrafting.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization");
    private final IDrawable icon;
    private IGuiHelper guiHelper;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;


    public JeiMiniaturizationCraftingCategory(IGuiHelper guiHelper) {
        int width = (9 * 18) + 10;
        int height = 10 + (18 * 3) + 5;

        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(width, height);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get()));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends MiniaturizationRecipe> getRecipeClass() {
        return MiniaturizationRecipe.class;
    }

    @Override
    public String getTitle() {
        return I18n.format(CompactCrafting.MOD_ID + ".jei.miniaturization.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setIngredients(MiniaturizationRecipe recipe, IIngredients ing) {
        List<ItemStack> inputs = new ArrayList<>();

        for (String compKey : recipe.getComponentKeys()) {
            Optional<BlockState> requiredBlock = recipe.getRecipeComponent(compKey);
            requiredBlock.ifPresent(bs -> {
                Item bi = Item.getItemFromBlock(bs.getBlock());
                inputs.add(new ItemStack(bi));
            });
        }

        inputs.add(recipe.getCatalyst());

        ing.setInputs(VanillaTypes.ITEM, inputs);
        ing.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.getOutputs()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MiniaturizationRecipe recipe, IIngredients iIngredients) {

        int GUTTER_X = 5;
        int OFFSET_Y = 10;

        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        int numComponentSlots = 18;

        addMaterialSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

        int catalystSlot = addCatalystSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

        addOutputSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

        guiItemStacks.addTooltipCallback((slot, b, itemStack, tooltip) -> {
            if (slot >= 0 && slot < recipe.getComponentKeys().size()) {
                IFormattableTextComponent text =
                        new TranslationTextComponent(CompactCrafting.MOD_ID + ".jei.miniaturization.component")
                                .mergeStyle(TextFormatting.GRAY)
                                .mergeStyle(TextFormatting.ITALIC);

                tooltip.add(text);
            }

            if (slot == catalystSlot) {
                IFormattableTextComponent text = new TranslationTextComponent(CompactCrafting.MOD_ID + ".jei.miniaturization.catalyst")
                        .mergeStyle(TextFormatting.YELLOW)
                        .mergeStyle(TextFormatting.ITALIC);

                tooltip.add(text);
            }
        });
    }

    private int addCatalystSlots(MiniaturizationRecipe recipe, int GUTTER_X, int OFFSET_Y, IGuiItemStackGroup guiItemStacks, int numComponentSlots) {
        int catalystSlot = numComponentSlots + 5 + 1;
        guiItemStacks.init(catalystSlot, true, GUTTER_X, OFFSET_Y);
        guiItemStacks.set(catalystSlot, recipe.getCatalyst());
        guiItemStacks.setBackground(catalystSlot, slotDrawable);
        return catalystSlot;
    }

    private void addMaterialSlots(MiniaturizationRecipe recipe, int GUTTER_X, int OFFSET_Y, IGuiItemStackGroup guiItemStacks, int numComponentSlots) {
        for (int slot = 0; slot < numComponentSlots; slot++) {
            int slotX = GUTTER_X + (slot % 9) * 18;
            int slotY = (OFFSET_Y + 24) + ((slot / 9) * 18);

            guiItemStacks.init(slot, true, slotX, slotY);
            guiItemStacks.setBackground(slot, this.slotDrawable);
        }

        AtomicInteger inputOffset = new AtomicInteger();
        recipe.getRecipeComponentTotals()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach((comp) -> {
                    String component = comp.getKey();
                    int required = comp.getValue();
                    int finalInputOffset = inputOffset.get();

                    BlockState bs = recipe.getRecipeComponent(component).get();
                    Item bi = Item.getItemFromBlock(bs.getBlock());
                    guiItemStacks.set(finalInputOffset, new ItemStack(bi, required));

                    inputOffset.getAndIncrement();
                });
    }

    private void addOutputSlots(MiniaturizationRecipe recipe, int GUTTER_X, int OFFSET_Y, IGuiItemStackGroup guiItemStacks, int numComponentSlots) {
        int outputOffset = numComponentSlots;
        for (int outputNum = 0; outputNum < 5; outputNum++) {
            guiItemStacks.init(outputOffset + outputNum, false, GUTTER_X + (outputNum * 18) + (4 * 18), OFFSET_Y);
            guiItemStacks.setBackground(outputOffset + outputNum, this.slotDrawable);
        }

        for (ItemStack output : recipe.getOutputs()) {
            guiItemStacks.set(outputOffset, output);
        }
    }

    @Override
    public void draw(MiniaturizationRecipe recipe, MatrixStack mx, double mouseX, double mouseY) {
        mx.push();
        // mx.scale(3, 3, 1);
        // this.getIcon().draw(mx, 7, 0);
        mx.pop();
    }
}
