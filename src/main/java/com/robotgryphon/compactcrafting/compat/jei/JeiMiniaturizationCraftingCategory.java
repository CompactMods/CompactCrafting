package com.robotgryphon.compactcrafting.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import com.robotgryphon.compactcrafting.api.components.IRecipeBlockComponent;
import com.robotgryphon.compactcrafting.api.components.IRecipeComponents;
import com.robotgryphon.compactcrafting.api.layers.IRecipeLayer;
import com.robotgryphon.compactcrafting.client.fakeworld.RenderingWorld;
import com.robotgryphon.compactcrafting.projector.render.CCRenderTypes;
import com.robotgryphon.compactcrafting.recipes.MiniaturizationRecipe;
import com.robotgryphon.compactcrafting.recipes.components.BlockComponent;
import com.robotgryphon.compactcrafting.ui.ScreenArea;
import com.robotgryphon.compactcrafting.util.BlockSpaceUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization");
    private final IDrawable icon;
    private final BlockRendererDispatcher blocks;
    private RenderingWorld previewLevel;

    private IGuiHelper guiHelper;
    private final IDrawableStatic background;
    private final IDrawableStatic slotDrawable;
    private final IDrawableStatic arrowOutputs;

    private boolean singleLayer = false;
    private int singleLayerOffset = 0;
    private boolean debugMode = false;

    private ScreenArea explodeToggle = new ScreenArea(30, 75, 10, 10);
    private ScreenArea layerUp = new ScreenArea(55, 75, 10, 10);
    private ScreenArea layerSwap = new ScreenArea(70, 75, 10, 10);
    private ScreenArea layerDown = new ScreenArea(85, 75, 10, 10);

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
        int height = 60 + (10 + (18 * 3) + 5);

        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(width, height);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Registration.FIELD_PROJECTOR_BLOCK.get()));
        this.arrowOutputs = guiHelper.createDrawable(new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-outputs.png"), 0, 0, 24, 19);

        this.blocks = Minecraft.getInstance().getBlockRenderer();
        this.previewLevel = null;
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
        List<ItemStack> inputs = new ArrayList<>();

        IRecipeComponents components = recipe.getComponents();
        for (String compKey : components.getBlockComponents().keySet()) {
            Optional<IRecipeBlockComponent> requiredBlock = components.getBlock(compKey);
            requiredBlock.ifPresent(bs -> {
                // TODO - Abstract this better, need to be more flexible for other component types in the future
                if (bs instanceof BlockComponent) {
                    BlockComponent bsc = (BlockComponent) bs;
                    Item bi = bsc.getBlock().asItem();
                    if (bi != Items.AIR)
                        inputs.add(new ItemStack(bi));
                }
            });
        }

        inputs.add(recipe.getCatalyst());

        ing.setInputs(VanillaTypes.ITEM, inputs);
        ing.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.getOutputs()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MiniaturizationRecipe recipe, IIngredients iIngredients) {
        previewLevel = new RenderingWorld(recipe);

        singleLayer = false;
        singleLayerOffset = 0;

        int GUTTER_X = 5;
        int OFFSET_Y = 65;

        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
        int numComponentSlots = 18;
        int catalystSlot = -1;
        try {
            addMaterialSlots(recipe, GUTTER_X, OFFSET_Y, guiItemStacks, numComponentSlots);

            catalystSlot = addCatalystSlots(recipe, 0, 0, guiItemStacks, numComponentSlots);
            int fromRightEdge = this.background.getWidth() - (18 * 2) - GUTTER_X;
            addOutputSlots(recipe, fromRightEdge, 8, guiItemStacks, numComponentSlots);
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error(recipe.getId());
            CompactCrafting.LOGGER.error("Error displaying recipe", ex);
        }

        int finalCatalystSlot = catalystSlot;
        guiItemStacks.addTooltipCallback((slot, b, itemStack, tooltip) -> {
            if (slot >= 0 && slot < recipe.getComponents().size()) {
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
                    if (bs instanceof BlockComponent) {
                        BlockComponent bsc = (BlockComponent) bs;
                        Item bi = bsc.getBlock().asItem();
                        if (bi != Items.AIR) {
                            guiItemStacks.set(finalInputOffset, new ItemStack(bi, required));
                            inputOffset.getAndIncrement();
                        }
                    }
                });
    }

    private void addOutputSlots(MiniaturizationRecipe recipe, int GUTTER_X, int OFFSET_Y, IGuiItemStackGroup guiItemStacks, int numComponentSlots) {
        int outputOffset = numComponentSlots;
        for (int outputNum = 0; outputNum < 6; outputNum++) {
            int x = (18 * (outputNum % 2)) + GUTTER_X;
            int y = (18 * (outputNum / 2)) + OFFSET_Y;
            guiItemStacks.init(outputOffset + outputNum, false, x, y);
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
            ResourceLocation texture,
            ScreenArea area,
            float u, float v,
            int uWidth, int vHeight,
            int textureWidth, int textureHeight) {

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(texture);

        RenderSystem.enableDepthTest();
        AbstractGui.blit(matrixStack, area.x, area.y, area.width, area.height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    //endregion

    @Override
    public void draw(MiniaturizationRecipe recipe, MatrixStack mx, double mouseX, double mouseY) {
        AxisAlignedBB dims = recipe.getDimensions();

        MainWindow mainWindow = Minecraft.getInstance().getWindow();

        drawScaledTexture(mx,
                new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-field.png"),
                new ScreenArea(7, 20, 17, 22),
                0, 0, 17, 22, 17, 22);

        drawScaledTexture(mx,
                new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-outputs.png"),
                new ScreenArea(100, 25, 24, 19),
                0, 0, 24, 19, 24, 19);

        int scissorX = 27;
        int scissorY = 0;

        double guiScaleFactor = mainWindow.getGuiScale();
        ScreenArea scissorBounds = new ScreenArea(
                scissorX, scissorY,
                70,
                70
        );

        renderPreviewControls(mx, dims);

        if (previewLevel != null) renderRecipe(recipe, mx, dims, guiScaleFactor, scissorBounds);
    }

    private void renderRecipe(MiniaturizationRecipe recipe, MatrixStack mx, AxisAlignedBB dims, double guiScaleFactor, ScreenArea scissorBounds) {
        try {
            AbstractGui.fill(
                    mx,
                     scissorBounds.x, scissorBounds.y,
                    scissorBounds.x + scissorBounds.width,
                    scissorBounds.height,
                    0xFF404040
            );

            IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().renderBuffers().bufferSource();

            final double scale = Minecraft.getInstance().getWindow().getGuiScale();
            final Matrix4f matrix = mx.last().pose();
            final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            matrix.store(buf);

            // { x, y, z }
            Vector3d translation = new Vector3d(
                    buf.get(12) * scale,
                    buf.get(13) * scale,
                    buf.get(14) * scale);

            scissorBounds.x *= scale;
            scissorBounds.y *= scale;
            scissorBounds.width *= scale;
            scissorBounds.height *= scale;
            final int scissorX = Math.round(Math.round(translation.x + scissorBounds.x));
            final int scissorY = Math.round(Math.round(Minecraft.getInstance().getWindow().getHeight() - scissorBounds.y - scissorBounds.height - translation.y));
            final int scissorW = Math.round(scissorBounds.width);
            final int scissorH = Math.round(scissorBounds.height);
            RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);

            mx.pushPose();

            mx.translate(
                    27 + (35),
                    scissorBounds.y + (35),
                    100);

            // 13 = 1
            // 11 = 3
            // 09 = 5
            // 07 = 7
            // 05 = 9
            // 03 = 11
            // 01 = 13

            Vector3d dimsVec = new Vector3d(dims.getXsize(), dims.getYsize(), dims.getZsize());
            float recipeAvgDim = (float) dimsVec.length();
            float previewScale = (float) ((3 + Math.exp(3 - (recipeAvgDim / 5))) / explodeMulti);
            mx.scale(previewScale, -previewScale, previewScale);

            drawActualRecipe(recipe, mx, dims, buffers);

            mx.popPose();

            buffers.endBatch();

            RenderSystem.disableScissor();
        } catch (Exception ex) {
            CompactCrafting.LOGGER.warn(ex);
        }
    }

    private void drawActualRecipe(MiniaturizationRecipe recipe, MatrixStack mx, AxisAlignedBB dims, IRenderTypeBuffer.Impl buffers) {
        double gameTime = Minecraft.getInstance().level.getGameTime();
        double test = Math.toDegrees(gameTime) / 15;
        mx.mulPose(new Quaternion(35f,
                (float) -test,
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

        for (int y : renderLayers) {
            recipe.getLayer(y).ifPresent(l -> renderRecipeLayer(recipe, mx, buffers, l, y));
        }
    }

    private void renderPreviewControls(MatrixStack mx, AxisAlignedBB dims) {
        mx.pushPose();
        mx.translate(0, 0, 10);

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
    }

    private void renderRecipeLayer(MiniaturizationRecipe recipe, MatrixStack mx, IRenderTypeBuffer.Impl buffers, IRecipeLayer l, int layerY) {
        // Begin layer
        mx.pushPose();

        AxisAlignedBB layerBounds = BlockSpaceUtil.getLayerBoundsByYOffset(recipe.getDimensions(), layerY);
        BlockPos.betweenClosedStream(layerBounds).forEach(filledPos -> {
            mx.pushPose();

            mx.translate(
                    ((filledPos.getX() + 0.5) * explodeMulti),
                    ((layerY + 0.5) * explodeMulti),
                    ((filledPos.getZ() + 0.5) * explodeMulti)
            );

            BlockPos zeroedPos = filledPos.below(layerY);
            Optional<String> componentForPosition = l.getComponentForPosition(zeroedPos);
            componentForPosition
                    .flatMap(recipe.getComponents()::getBlock)
                    .ifPresent(comp -> renderComponent(mx, buffers, comp, filledPos));

            mx.popPose();
        });

        // Done with layer
        mx.popPose();
    }

    private void renderComponent(MatrixStack mx, IRenderTypeBuffer.Impl buffers, IRecipeBlockComponent state, BlockPos filledPos) {
        // TODO - Render switching at fixed interval
        if (state.didErrorRendering())
            return;

        BlockState state1 = state.getRenderState();
        // Thanks Immersive, Astral, and others
        IRenderTypeBuffer light = CCRenderTypes.disableLighting(buffers);

        IModelData data = EmptyModelData.INSTANCE;
        if (previewLevel != null && state1.hasTileEntity()) {
            // create fake world instance
            // get tile entity - extend EmptyBlockReader with impl
            TileEntity be = previewLevel.getBlockEntity(filledPos);
            if (be != null)
                data = be.getModelData();
        }

        try {
            blocks.renderBlock(state1,
                    mx,
                    light,
                    0xf000f0,
                    OverlayTexture.NO_OVERLAY,
                    data);
        } catch (Exception e) {
            state.markRenderingErrored();

            CompactCrafting.LOGGER.warn("Error rendering block in preview: {}", state1.getBlock().getRegistryName());
            CompactCrafting.LOGGER.error("Stack Trace", e);
        }
    }
}
