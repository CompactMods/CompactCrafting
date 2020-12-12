package com.robotgryphon.compactcrafting.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.client.render.RenderTickCounter;
import com.robotgryphon.compactcrafting.client.render.RenderTypesExtensions;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.layers.IRecipeLayer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private Rectangle layerUp = new Rectangle(0, 12, 10, 10);
    private Rectangle layerSwap = new Rectangle(0, 23, 10, 10);
    private Rectangle layerDown = new Rectangle(0, 34, 10, 10);

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

        this.blocks = Minecraft.getInstance().getBlockRendererDispatcher();
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

        singleLayer = false;
        singleLayerOffset = 0;

        int GUTTER_X = 5;
        int OFFSET_Y = 150;

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
    public boolean handleClick(MiniaturizationRecipe recipe, double mouseX, double mouseY, int mouseButton) {

        if (explodeToggle.contains(mouseX, mouseY)) {
            explodeMulti = exploded ? 1.0d : 1.6d;
            exploded = !exploded;
            return true;
        }

        if (layerSwap.contains(mouseX, mouseY)) {
            singleLayer = !singleLayer;
            return true;
        }

        if (layerUp.contains(mouseX, mouseY) && singleLayer) {
            if(singleLayerOffset < recipe.getDimensions().getYSize() - 1)
                singleLayerOffset++;

            return true;
        }

        if (layerDown.contains(mouseX, mouseY) && singleLayer) {
            if(singleLayerOffset > 0)
                singleLayerOffset--;

            return true;
        }

        return false;
    }

    @Override
    public void draw(MiniaturizationRecipe recipe, MatrixStack mx, double mouseX, double mouseY) {

//        try {
//            IDrawableBuilder b = guiHelper.drawableBuilder(new ResourceLocation(CompactCrafting.MOD_ID, "block/field_projector"), 16, 16, 16, 16);
//            IDrawableStatic build = b.build();
//
//            build.draw(mx, 0, 30);
//        } catch (Exception ex) {
//        }

        AxisAlignedBB dims = recipe.getDimensions();

        IDrawableStatic jei = guiHelper
                .drawableBuilder(
                new ResourceLocation(CompactCrafting.MOD_ID, "textures/nope.png"),
                0, 0,
                10, 10
        ).setTextureSize(16, 16).build();

        jei.draw(mx, explodeToggle.x, explodeToggle.y);

        jei.draw(mx, layerSwap.x, layerSwap.y);
        if(singleLayer) {
            if(singleLayerOffset < dims.getYSize() - 1)
                jei.draw(mx, layerUp.x, layerUp.y);

            if(singleLayerOffset > 0)
                jei.draw(mx, layerDown.x, layerDown.y);
        }

        try {
            IRenderTypeBuffer.Impl buffers = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
            IVertexBuilder field = buffers.getBuffer(RenderTypesExtensions.PROJECTION_FIELD_RENDERTYPE);
            IVertexBuilder lines = buffers.getBuffer(RenderType.getLines());

//            drawRect(mx, lines, layerUp, Color.blue);
//            drawRect(mx, lines, layerSwap, Color.red);
//            drawRect(mx, lines, layerDown, Color.blue);

            mx.push();

            // mx.translate(-dims.getXSize()/2, -dims.getYSize() /2 , -dims.getZSize() / 2);

            mx.translate(
                    background.getWidth() / 2,
                    70,
                    400);

            mx.scale(10, -10, 10);

            mx.rotate(new Quaternion(35f,
                    -(RenderTickCounter.renderTicks),
                    0,
                    true));

            if (debugMode) {
                // DEBUG Line
                addColoredVertex(lines, mx, Color.RED, new Vector3f(0, (float) -10, 0));
                addColoredVertex(lines, mx, Color.RED, new Vector3f(0, (float) 10, 0));
            }

            double ySize = recipe.getDimensions().getYSize();

            // Variable explode based on mouse position (clamped)
            // double explodeMulti = MathHelper.clamp(mouseX, 0, this.background.getWidth())/this.background.getWidth()*2+1;


            int[] renderLayers;
            if (!singleLayer) {
                renderLayers = IntStream.range(0, (int) ySize).toArray();
            } else {
                renderLayers = new int[]{singleLayerOffset};
            }

            mx.translate(
                    -(dims.getXSize() / 2) * explodeMulti - 0.5,
                    -(dims.getYSize() / 2) * explodeMulti - 0.5,
                    -(dims.getZSize() / 2) * explodeMulti - 0.5
            );

            for (int y : renderLayers) {
                Optional<IRecipeLayer> layer = recipe.getLayer(y);
                layer.ifPresent(l -> renderRecipeLayer(recipe, mx, buffers, l, y));
            }

            mx.pop();

            buffers.finish();
        } catch (Exception ex) {
            CompactCrafting.LOGGER.warn(ex);
        }
    }

    private void drawRect(MatrixStack mx, IVertexBuilder field, Rectangle rect, Color color) {
        mx.push();
        mx.translate(rect.getX(), rect.getY(), 0);
        addColoredVertex(field, mx, color, new Vector3d(rect.getX(), rect.getY(), 0));
        addColoredVertex(field, mx, color, new Vector3d(rect.getX() + rect.getWidth(), rect.getY(), 0));

        addColoredVertex(field, mx, color, new Vector3d(rect.getX() + rect.getWidth(), rect.getY(), 0));
        addColoredVertex(field, mx, color, new Vector3d(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), 0));

        addColoredVertex(field, mx, color, new Vector3d(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), 0));
        addColoredVertex(field, mx, color, new Vector3d(rect.getX(), rect.getY() + rect.getHeight(), 0));

        addColoredVertex(field, mx, color, new Vector3d(rect.getX(), rect.getY() + rect.getHeight(), 0));
        addColoredVertex(field, mx, color, new Vector3d(rect.getX(), rect.getY(), 0));
        mx.pop();
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, Vector3d position) {
        renderer.pos(position.getX(), position.getY(), position.getZ())
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .lightmap(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void addColoredVertex(IVertexBuilder renderer, MatrixStack stack, Color color, Vector3f position) {
        renderer.pos(position.getX(), position.getY(), position.getZ())
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .lightmap(0, 240)
                .normal(1, 0, 0)
                .endVertex();
    }

    private void renderRecipeLayer(MiniaturizationRecipe recipe, MatrixStack mx, IRenderTypeBuffer.Impl buffers, IRecipeLayer l, int layerY) {
        // Begin layer
        mx.push();

        for (BlockPos filledPos : l.getNonAirPositions()) {
            mx.push();

            mx.translate(
                    ((filledPos.getX() + 0.5) * explodeMulti),
                    ((layerY + 0.5) * explodeMulti),
                    ((filledPos.getZ() + 0.5) * explodeMulti)
            );

            String component = l.getRequiredComponentKeyForPosition(filledPos);
            Optional<BlockState> recipeComponent = recipe.getRecipeComponent(component);

            recipeComponent.ifPresent(state -> {
                // renderer.render(renderTe, pos.getX(), pos.getY(), pos.getZ(), 0.0f);

                // 0xf000f0
                blocks.renderBlock(state, mx, buffers,
                        0xf000f0, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
            });

            mx.pop();
        }

        // Done with layer
        mx.pop();
    }
}
