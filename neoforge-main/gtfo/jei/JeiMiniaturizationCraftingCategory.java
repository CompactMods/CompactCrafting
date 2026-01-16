package dev.compactmods.crafting.compat.jei;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import dev.compactmods.gander.level.VirtualLevel;
import dev.compactmods.gander.render.geometry.BakedLevel;
import dev.compactmods.gander.render.geometry.LevelBakery;
import dev.compactmods.gander.ui.widget.SpatialRenderer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final Identifier UID = CompactCrafting.modRL("miniaturization");
    public static final RecipeType<MiniaturizationRecipe> RECIPE_TYPE = new RecipeType<>(UID, MiniaturizationRecipe.class);

    private final IDrawable icon;
    private final BlockRenderDispatcher blocks;
    private final RegistryAccess registryAccess;
    private BakedLevel level;

    private IGuiHelper guiHelper;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic arrowOutputs;

    private boolean singleLayer = false;
    private int singleLayerOffset = 0;
    private boolean debugMode = false;

    private ScreenRectangle explodeToggle = new ScreenRectangle(30, 75, 10, 10);
    private ScreenRectangle layerUp = new ScreenRectangle(55, 75, 10, 10);
    private ScreenRectangle layerSwap = new ScreenRectangle(70, 75, 10, 10);
    private ScreenRectangle layerDown = new ScreenRectangle(85, 75, 10, 10);

    /**
     * Whether the preview is exploded (expanded) or not.
     */
    private boolean exploded = false;

    /**
     * Explode multiplier; specifies how far apart blocks are rendered.
     */
    private double explodeMulti = 1.0d;

    private final MutableComponent MATERIAL_COMPONENT = Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.component")
            .withStyle(ChatFormatting.GRAY)
            .withStyle(ChatFormatting.ITALIC);

    private final MutableComponent CATALYST = Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.catalyst")
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(ChatFormatting.ITALIC);

    public JeiMiniaturizationCraftingCategory(IGuiHelper guiHelper, RegistryAccess registryAccess) {
        this.guiHelper = guiHelper;
        this.registryAccess = registryAccess;

        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CCBlocks.FIELD_PROJECTOR_BLOCK.get()));
        this.arrowOutputs = guiHelper.createDrawable(CompactCrafting.modRL("textures/gui/jei-arrow-outputs.png"), 0, 0, 24, 19);

        this.blocks = Minecraft.getInstance().getBlockRenderer();
        this.level = null;
    }

    @Override
    public RecipeType<MiniaturizationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    //region JEI implementation requirements
    @Override
    public Component getTitle() {
        return Component.translatable(CompactCrafting.MOD_ID + ".jei.miniaturization.title");
    }

    @Override
    public int getWidth() {
        return (9 * 18) + 10;
    }

    @Override
    public int getHeight() {
        return 60 + (10 + (18 * 3) + 5);
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    //endregion


    @Override
    public void setRecipe(IRecipeLayoutBuilder layout, MiniaturizationRecipe recipe, IFocusGroup focuses) {
        var previewLevel = new VirtualLevel(this.registryAccess, true);
        previewLevel.setBounds(recipe.dimensions());

        for (int y : recipe.layers().keySet()) {
            recipe.getLayer(y).ifPresent(l -> addLayerForRender(previewLevel, recipe, l, y));
        }

        this.level = LevelBakery.bakeVertices(previewLevel, recipe.dimensions(), new Vector3f());

        singleLayer = false;
        singleLayerOffset = 0;


        try {
            addMaterialSlots(recipe, layout);
            addCatalystSlots(recipe, layout);

            int fromRightEdge = this.getWidth() - (18 * 2) - 5;
            addOutputSlots(recipe, layout, fromRightEdge);
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error("Error displaying recipe", ex);
        }
    }

    private IRecipeSlotBuilder addCatalystSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout) {
        final var catalystSlot = layout.addSlot(RecipeIngredientRole.CATALYST, 1, 1)
                .setBackground(slotDrawable, -1, -1);

        // FIXME
//        var catalyst = recipe.catalystMatcher();
//        if (!catalyst.isEmpty()) {
//            catalystSlot.addItemStack(catalyst).addRichTooltipCallback((slots, c) -> c.add(CATALYST));
//        }

        return catalystSlot;
    }


    private void addMaterialSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout) {
        AtomicInteger inputOffset = new AtomicInteger();

        final int GUTTER_X = 5;
        final int OFFSET_Y = 64;

        recipe.getComponentTotals()
                .entrySet()
                .stream()
                .filter(comp -> comp.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach((comp) -> {
                    String component = comp.getKey();
                    int required = comp.getValue();
                    int finalInputOffset = inputOffset.get();

                    IRecipeBlockComponent bs = recipe.getComponents().getBlock(component).get();
                    if (bs instanceof BlockComponent bsc) {
                        Item bi = bsc.getBlock().asItem();

                        int slotX = GUTTER_X + (finalInputOffset % 9) * 18;
                        int slotY = (OFFSET_Y + 24) + ((finalInputOffset / 9) * 18);

                        final var slot = layout.addSlot(RecipeIngredientRole.INPUT, slotX, slotY)
                                .setBackground(slotDrawable, -1, -1);

                        if (bi != Items.AIR) {
                            slot.addItemStack(new ItemStack(bi, required));
                            slot.addRichTooltipCallback((slots, c) -> c.add(MATERIAL_COMPONENT));
                            inputOffset.getAndIncrement();
                        }
                    }
                });

        for (int i = inputOffset.get(); i < 18; i++) {
            int slotX = GUTTER_X + (i % 9) * 18;
            int slotY = (OFFSET_Y + 24) + ((i / 9) * 18);

            layout.addSlot(RecipeIngredientRole.INPUT, slotX, slotY).setBackground(slotDrawable, -1, -1);
        }
    }

    private void addOutputSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout, int GUTTER_X) {
        final var out = recipe.getOutputs();
        for (int outputNum = 0; outputNum < 6; outputNum++) {
            int x = (18 * (outputNum % 2)) + GUTTER_X + 1;
            int y = (18 * (outputNum / 2)) + 8 + 1;

            final var slot = layout.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .setBackground(slotDrawable, -1, -1);

            if (outputNum < out.length)
                slot.addItemStack(out[outputNum]);
        }
    }
    //endregion


    @Override
    public void getTooltip(ITooltipBuilder tooltip, MiniaturizationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (explodeToggle.containsPoint((int) mouseX, (int) mouseY)) {
            if (!exploded) tooltip.add(Component.translatable("compactcrafting.jei.toggle_exploded_view"));
            else tooltip.add(Component.translatable("compactcrafting.jei.toggle_condensed_view"));
        }

        if (layerSwap.containsPoint((int) mouseX, (int) mouseY)) {
            if (singleLayer) tooltip.add(Component.translatable("compactcrafting.jei.all_layers_mode"));
            else tooltip.add(Component.translatable("compactcrafting.jei.single_layer_mode"));
        }

        if (layerUp.containsPoint((int) mouseX, (int) mouseY) && singleLayer) {
            if (singleLayerOffset < recipe.getDimensions().getYsize() - 1)
                tooltip.add(Component.translatable("compactcrafting.jei.layer_up"));
        }

        if (layerDown.containsPoint((int) mouseX, (int) mouseY) && singleLayer) {
            if (singleLayerOffset > 0)
                tooltip.add(Component.translatable("compactcrafting.jei.layer_down"));
        }
    }

    @Override
    public boolean handleInput(MiniaturizationRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            SoundManager handler = Minecraft.getInstance().getSoundManager();

            if (explodeToggle.containsPoint((int) mouseX, (int) mouseY)) {
                explodeMulti = exploded ? 1.0d : 1.6d;
                exploded = !exploded;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerSwap.containsPoint((int) mouseX, (int) mouseY)) {
                singleLayer = !singleLayer;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerUp.containsPoint((int) mouseX, (int) mouseY) && singleLayer) {
                if (singleLayerOffset < recipe.getDimensions().getYsize() - 1) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    singleLayerOffset++;
                }
                return true;
            }

            if (layerDown.containsPoint((int) mouseX, (int) mouseY) && singleLayer) {
                if (singleLayerOffset > 0) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    singleLayerOffset--;
                }

                return true;
            }
        }

        return false;
    }

    //region Rendering help
    private void drawScaledTexture(
            GuiGraphics guiGraphics,
            Identifier texture,
            ScreenRectangle area,
            float u, float v,
            int uWidth, int vHeight,
            int textureWidth, int textureHeight) {

        guiGraphics.blit(texture, area.left(), area.top(), area.width(), area.height(), u, v,
                uWidth, vHeight, textureWidth, textureHeight);
    }

    //endregion

    @Override
    public void draw(MiniaturizationRecipe recipe, IRecipeSlotsView slots, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        AABB dims = recipe.getDimensions();

        drawScaledTexture(guiGraphics,
                CompactCrafting.modRL("textures/gui/jei-arrow-field.png"),
                new ScreenRectangle(7, 20, 17, 22),
                0, 0, 17, 22, 17, 22);

        drawScaledTexture(guiGraphics,
                CompactCrafting.modRL("textures/gui/jei-arrow-outputs.png"),
                new ScreenRectangle(100, 25, 24, 19),
                0, 0, 24, 19, 24, 19);

        renderPreviewControls(guiGraphics, dims);

        var partialTicks = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();

        var renderer = new SpatialRenderer(this.level, 0, 0, getWidth(), getHeight());
        renderer.camera().zoom(-1.0f * (float) Math.sqrt(Math.pow(recipe.dimensions().getXsize(), 2) * 3));
        renderer.camera().lookUp(3 / 12f);
        renderer.render(guiGraphics, (int) mouseX, (int) mouseY, partialTicks);
    }

    private void renderPreviewControls(GuiGraphics guiGraphics, AABB dims) {
        PoseStack mx = guiGraphics.pose();
        mx.pushPose();
        mx.translate(0, 0, 10);

        Identifier sprites = CompactCrafting.modRL("textures/gui/jei-sprites.png");

        if (exploded) {
            drawScaledTexture(guiGraphics, sprites, explodeToggle, 20, 0, 20, 20, 120, 20);
        } else {
            drawScaledTexture(guiGraphics, sprites, explodeToggle, 0, 0, 20, 20, 120, 20);
        }

        if (singleLayer) {
            drawScaledTexture(guiGraphics, sprites, layerSwap, 60, 0, 20, 20, 120, 20);
        } else {
            drawScaledTexture(guiGraphics, sprites, layerSwap, 40, 0, 20, 20, 120, 20);
        }

        if (singleLayer) {
            if (singleLayerOffset < dims.getYsize() - 1)
                drawScaledTexture(guiGraphics, sprites, layerUp, 80, 0, 20, 20, 120, 20);

            if (singleLayerOffset > 0) {
                drawScaledTexture(guiGraphics, sprites, layerDown, 100, 0, 20, 20, 120, 20);
            }
        }

        mx.popPose();
    }

    private void addLayerForRender(VirtualLevel level, MiniaturizationRecipe recipe, IRecipeLayer l, int layerY) {
        // Begin layer
        AABB layerBounds = BlockSpaceUtil.getLayerBounds(recipe.getDimensions(), layerY);
        BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
            BlockPos zeroedPos = filledPos.below(layerY);
            Optional<String> componentForPosition = l.getComponentForPosition(zeroedPos);
            componentForPosition
                    .flatMap(recipe.getComponents()::getBlock)
                    .ifPresent(comp -> level.blockSystem().blockAndFluidStorage().setBlockState(zeroedPos, comp.getRenderState()));
        });
    }
}