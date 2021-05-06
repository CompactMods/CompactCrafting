package com.robotgryphon.compactcrafting.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.render.CCRenderTypes;
import com.robotgryphon.compactcrafting.client.render.RenderTickCounter;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.recipes.components.RecipeBlockStateComponent;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization");
    private final IDrawable icon;
    private final BlockRendererDispatcher blocks;
    private IGuiHelper guiHelper;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;
    private boolean singleLayer = false;
    private int singleLayerOffset = 0;
    private boolean debugMode = false;

    private Rectangle explodeToggle = new Rectangle(0, 0, 10, 10);
    private Rectangle layerUp = new Rectangle(0, 25, 10, 10);
    private Rectangle layerSwap = new Rectangle(0, 37, 10, 10);
    private Rectangle layerDown = new Rectangle(0, 49, 10, 10);

    /**
     * Whether or not the preview is exploded (expanded) or not.
     */
    private boolean exploded = false;

    /**
     * Explode multiplier; specifies how far apart blocks are rendered.
     */
    private double explodeMulti = 1.0d;

    public JeiMiniaturizationCraftingCategory(IGuiHelper guiHelper) {
        int width = (9 * 18) + 10;
        int height = 150 + (10 + (18 * 3) + 5);

        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(width, height);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get()));

        this.blocks = Minecraft.getInstance().getBlockRenderer();
    }

    //region JEI implementation requirements
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
        return I18n.get(CompactCrafting.MOD_ID + ".jei.miniaturization.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    //endregion

    //region Recipe Slots and Items
    @Override
    public void setIngredients(MiniaturizationRecipe recipe, IIngredients ing) {
        Set<Block> blockInputs = new LinkedHashSet<>();

        for (String compKey : recipe.getComponentKeys()) {
            Optional<RecipeBlockStateComponent> requiredBlock = recipe.getRecipeBlockComponent(compKey);
            requiredBlock.map(RecipeBlockStateComponent::getBlock).ifPresent(blockInputs::add);
        }

        List<ItemStack> inputs = new ArrayList<>();
        for (Block block : blockInputs) {
            inputs.add(new ItemStack(block));
        }
        inputs.add(recipe.getCatalyst());

        ing.setInputs(VanillaTypes.ITEM, inputs);
        ing.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MiniaturizationRecipe recipe, IIngredients iIngredients) {
        singleLayer = false;
        singleLayerOffset = 0;

        int GUTTER_X = 5;
        int OFFSET_Y = 150;

        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        int numComponentSlots = 18;
        int catalystSlot = -1;
        try {
            addMaterialSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

            catalystSlot = addCatalystSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

            addOutputSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error(recipe.getId());
            CompactCrafting.LOGGER.error("Error displaying recipe", ex);
        }

        int finalCatalystSlot = catalystSlot;
        guiItemStacks.addTooltipCallback((slot, b, itemStack, tooltip) -> {
            if (slot >= 0 && slot < recipe.getComponentKeys().size()) {
                IFormattableTextComponent text =
                        new TranslationTextComponent(CompactCrafting.MOD_ID + ".jei.miniaturization.component")
                                .withStyle(TextFormatting.GRAY)
                                .withStyle(TextFormatting.ITALIC);

                tooltip.add(text);
            }

            if (slot == finalCatalystSlot) {
                IFormattableTextComponent text = new TranslationTextComponent(CompactCrafting.MOD_ID + ".jei.miniaturization.catalyst")
                        .withStyle(TextFormatting.YELLOW)
                        .withStyle(TextFormatting.ITALIC);

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
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(e -> recipe.getRecipeBlockComponent(e.getKey()).get().getBlock(), Map.Entry::getValue, Integer::sum, LinkedHashMap::new))
                .forEach((block, amount) -> guiItemStacks.set(inputOffset.getAndIncrement(), new ItemStack(block, amount)));
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
    //endregion

    @Override
    public boolean handleClick(MiniaturizationRecipe recipe, double mouseX, double mouseY, int mouseButton) {

        SoundHandler handler = Minecraft.getInstance().getSoundManager();


        if (explodeToggle.contains(mouseX, mouseY)) {
            explodeMulti = exploded ? 1.0d : 1.6d;
            exploded = !exploded;
            handler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        if (layerSwap.contains(mouseX, mouseY)) {
            singleLayer = !singleLayer;
            handler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        if (layerUp.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset < recipe.getDimensions().getYsize() - 1) {
                handler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                singleLayerOffset++;
            }

            return true;
        }

        if (layerDown.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset > 0) {
                handler.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                singleLayerOffset--;
            }

            return true;
        }

        return false;
    }

    //region Rendering help
    private void drawScaledTexture(
            MatrixStack matrixStack,
            ResourceLocation texture, Rectangle bounds,
            float u, float v,
            int uWidth, int vHeight,
            int textureWidth, int textureHeight) {

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(texture);

        RenderSystem.enableDepthTest();
        Screen curr = Minecraft.getInstance().screen;
        AbstractGui.blit(matrixStack, bounds.x, bounds.y, bounds.width, bounds.height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
//        if (this.isHovered()) {
//            this.renderToolTip(matrixStack, mouseX, mouseY);
//        }
    }

    private void addColoredVertex(IVertexBuilder renderer, Color color, Vector3d position) {
        renderer.vertex(position.x(), position.y(), position.z())
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .uv2(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void drawRectOutline(IVertexBuilder renderer, Color color, Rectangle rect) {
        Vector3d bottomLeft = new Vector3d((float) rect.getMinX(), (float) rect.getMinY(), 0);
        Vector3d bottomRight = bottomLeft.add(rect.width, 0, 0);
        Vector3d topRight = bottomLeft.add(rect.width, rect.height, 0);
        Vector3d topLeft = bottomLeft.add(0, rect.height, 0);

        addColoredVertex(renderer, color, bottomLeft);
        addColoredVertex(renderer, color, bottomRight);

        addColoredVertex(renderer, color, bottomRight);
        addColoredVertex(renderer, color, topRight);

        addColoredVertex(renderer, color, topRight);
        addColoredVertex(renderer, color, topLeft);

        addColoredVertex(renderer, color, topLeft);
        addColoredVertex(renderer, color, bottomLeft);
    }
    //endregion

    @Override
    public void draw(MiniaturizationRecipe recipe, MatrixStack mx, double mouseX, double mouseY) {
        AxisAlignedBB dims = recipe.getDimensions();

        Screen curr = Minecraft.getInstance().screen;

        MainWindow mainWindow = Minecraft.getInstance().getWindow();
        int scaledWidth = mainWindow.getGuiScaledWidth();
        int scaledHeight = mainWindow.getGuiScaledHeight();

        int winWidth = (9 * 18) + 10;

        int slotHeight = (18 * 3) - 8;

        int scissorX = (curr.width / 2) - (background.getWidth() / 2) + 15;
        int scissorY = (curr.height / 2) - (background.getHeight() / 2)  + slotHeight;

        double guiScaleFactor = mainWindow.getGuiScale();
        Rectangle scissorBounds = new Rectangle(
                scissorX, scissorY,
                winWidth - 22,
                (int) (background.getHeight() - slotHeight - 27)
        );

        //region JEI controls
        mx.pushPose();
        mx.translate(0, 0, 500);

        ResourceLocation sprites = new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-sprites.png");

        if (exploded) {
            drawScaledTexture(mx, sprites, explodeToggle, 20, 0, 20, 20, 120, 20);
        } else {
            drawScaledTexture(mx, sprites, explodeToggle, 0, 0, 20, 20, 120, 20);
        }

        // Layer change buttons
        if (singleLayer) {
            drawScaledTexture(mx, sprites, layerSwap, 60, 0, 20, 20, 120, 20);
        } else {
            drawScaledTexture(mx, sprites, layerSwap, 40, 0, 20, 20, 120, 20);
        }

        if (singleLayer) {
            if (singleLayerOffset < dims.getYsize() - 1)
                drawScaledTexture(mx, sprites, layerUp, 80, 0, 20, 20, 120, 20);

            if (singleLayerOffset > 0) {
                drawScaledTexture(mx, sprites, layerDown, 100, 0, 20, 20, 120, 20);
            }
        }

        mx.popPose();

        //endregion

        try {
            AbstractGui.fill(
                    mx,
                    // scissorBounds.x, scissorBounds.y,
                    14, 0,
                    scissorBounds.width + 16,
                    scissorBounds.height + 1,
                    Color.darkGray.getRGB()
            );

            IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());

            RenderSystem.enableScissor(
                    (int) (scissorBounds.x * guiScaleFactor),
                    (int) (scissorBounds.y * guiScaleFactor),
                    (int) (scissorBounds.width * guiScaleFactor),
                    (int) (scissorBounds.height * guiScaleFactor));

            mx.pushPose();

            mx.translate(
                    (background.getWidth() / 2) + 6,
                    70,
                    400);

            mx.scale(10, -10, 10);

            mx.mulPose(new Quaternion(35f,
                    -(RenderTickCounter.renderTicks),
                    0,
                    true));

            double ySize = recipe.getDimensions().getYsize();

            // Variable explode based on mouse position (clamped)
            // double explodeMulti = MathHelper.clamp(mouseX, 0, this.background.getWidth())/this.background.getWidth()*2+1;


            int[] renderLayers;
            if (!singleLayer) {
                renderLayers = IntStream.range(0, (int) ySize).toArray();
            } else {
                renderLayers = new int[]{singleLayerOffset};
            }

            mx.translate(
                    -(dims.getXsize() / 2) * explodeMulti - 0.5,
                    -(dims.getYsize() / 2) * explodeMulti - 0.5,
                    -(dims.getZsize() / 2) * explodeMulti - 0.5
            );

            // ZORN NO
            //if(exploded) {
            //    float s = 1 / (float) explodeMulti;
            //    mx.scale(s, s, s);
            //}

            for (int y : renderLayers) {
                Optional<IRecipeLayer> layer = recipe.getLayer(y);
                layer.ifPresent(l -> renderRecipeLayer(recipe, mx, buffers, l, y));
            }


            mx.popPose();

            buffers.endBatch();

            RenderSystem.disableScissor();
        } catch (Exception ex) {
            CompactCrafting.LOGGER.warn(ex);
        }
    }

    private void renderRecipeLayer(MiniaturizationRecipe recipe, MatrixStack mx, IRenderTypeBuffer.Impl buffers, IRecipeLayer l, int layerY) {
        // Begin layer
        mx.pushPose();

        for (BlockPos filledPos : l.getFilledPositions()) {
            mx.pushPose();

            mx.translate(
                    ((filledPos.getX() + 0.5) * explodeMulti),
                    ((layerY + 0.5) * explodeMulti),
                    ((filledPos.getZ() + 0.5) * explodeMulti)
            );

            String component = l.getRequiredComponentKeyForPosition(filledPos).get();
            Optional<RecipeBlockStateComponent> recipeComponent = recipe.getRecipeBlockComponent(component);

            recipeComponent.ifPresent(state -> {
                // renderer.render(renderTe, pos.getX(), pos.getY(), pos.getZ(), 0.0f);
                // TODO - Render switching at fixed interval
                BlockState state1 = state.getStateWithDirections();
                // Thanks Immersive, Astral, and others
                IRenderTypeBuffer light = CCRenderTypes.disableLighting(buffers);
                blocks.renderBlock(state1,
                        mx,
                        light,
                        0xf000f0,
                        OverlayTexture.NO_OVERLAY,
                        EmptyModelData.INSTANCE);
            });

            mx.popPose();
        }

        // Done with layer
        mx.popPose();
    }
}
