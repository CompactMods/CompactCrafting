package com.robotgryphon.compactcrafting.client.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.JsonOps;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.inventory.MiniaturizationRecipeContainer;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class MiniaturizationRecipeScreen extends ContainerScreen<MiniaturizationRecipeContainer> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/container/miniaturization_recipe.png");
    private static final ITextComponent TITLE_TEXT = new TranslationTextComponent("gui.compactcrafting.copy");
    private static final ITextComponent TOOLTIP_TEXT = new TranslationTextComponent("gui.compactcrafting.copy.tooltip");
    private static final ITextComponent COPIED_TEXT = new TranslationTextComponent("gui.compactcrafting.copy.success").withStyle(TextFormatting.GREEN);

    private Button copyButton;
    private long lastCopyTime;

    public MiniaturizationRecipeScreen(MiniaturizationRecipeContainer container, PlayerInventory playerInv, ITextComponent title) {
        super(container, playerInv, title);
        this.imageHeight = 226;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.isModifiable()) {
            int w = 40;
            int h = 20;
            int x = (this.width - this.imageWidth) / 2 - w - 5;
            int y = (this.height - this.imageHeight) / 2;
            copyButton = this.addButton(new Button(x, y, w, h, TITLE_TEXT,
                    b -> MiniaturizationRecipe.CODEC.encodeStart(JsonOps.INSTANCE, prepareRecipeCopy()).result()
                            .map(json -> {
                                JsonObject obj = json.getAsJsonObject();
                                obj.add("type", new JsonPrimitive(Registration.MINIATURIZATION_RECIPE_TYPE.toString()));
                                return obj;
                            })
                            .map(GSON::toJson)
                            .ifPresent(s -> {
                                this.minecraft.keyboardHandler.setClipboard(s);
                                this.lastCopyTime = Util.getMillis();
                            }), this::renderCopyButtonTooltip));
            copyButton.active = false;
        }
    }

    private void renderCopyButtonTooltip(Button button, MatrixStack matrixStack, int mouseX, int mouseY) {
        boolean recentlyCopied = Util.getMillis() - this.lastCopyTime <= 2_000L;
        this.renderTooltip(matrixStack, recentlyCopied ? COPIED_TEXT : TOOLTIP_TEXT, mouseX, mouseY);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        if (copyButton != null)
            this.copyButton.active = this.menu.isCopyValid();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    private MiniaturizationRecipe prepareRecipeCopy() {
        MiniaturizationRecipe recipe = this.menu.getRecipe();
        recipe.getOutputs().removeIf(ItemStack.EMPTY::equals);
        return recipe;
    }
}
