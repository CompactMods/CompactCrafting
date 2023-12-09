package dev.compactmods.crafting.compat.jei;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.components.IRecipeBlockComponent;
import dev.compactmods.crafting.api.recipe.layers.IRecipeLayer;
import dev.compactmods.crafting.client.fakeworld.RenderingWorld;
import dev.compactmods.crafting.client.ui.ScreenArea;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.recipes.MiniaturizationRecipe;
import dev.compactmods.crafting.recipes.components.BlockComponent;
import dev.compactmods.crafting.util.BlockSpaceUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.lwjgl.BufferUtils;

public class JeiMiniaturizationCraftingCategory implements IRecipeCategory<MiniaturizationRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(CompactCrafting.MOD_ID, "miniaturization");
    public static final RecipeType<MiniaturizationRecipe> RECIPE_TYPE = new RecipeType<>(UID, MiniaturizationRecipe.class);

    private final IDrawable icon;
    private final BlockRenderDispatcher blocks;
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

    public JeiMiniaturizationCraftingCategory(IGuiHelper guiHelper) {
        int width = (9 * 18) + 10;
        int height = 60 + (10 + (18 * 3) + 5);

        this.guiHelper = guiHelper;
        this.background = guiHelper.createBlankDrawable(width, height);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(CCBlocks.FIELD_PROJECTOR_BLOCK.get()));
        this.arrowOutputs = guiHelper.createDrawable(new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-outputs.png"), 0, 0, 24, 19);

        this.blocks = Minecraft.getInstance().getBlockRenderer();
        this.previewLevel = null;
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }
    //endregion


    @Override
    public void setRecipe(IRecipeLayoutBuilder layout, MiniaturizationRecipe recipe, IFocusGroup focuses) {
        previewLevel = new RenderingWorld(recipe);

        singleLayer = false;
        singleLayerOffset = 0;

        try {
            addMaterialSlots(recipe, layout);
            addCatalystSlots(recipe, layout);

            int fromRightEdge = this.background.getWidth() - (18 * 2) - 5;
            addOutputSlots(recipe, layout, fromRightEdge);
        } catch (Exception ex) {
            CompactCrafting.LOGGER.error(recipe.getRecipeIdentifier());
            CompactCrafting.LOGGER.error("Error displaying recipe", ex);
        }
    }

    private IRecipeSlotBuilder addCatalystSlots(MiniaturizationRecipe recipe, IRecipeLayoutBuilder layout) {
        final var catalystSlot = layout.addSlot(RecipeIngredientRole.CATALYST, 1, 1)
                .setBackground(slotDrawable, -1, -1);

        if (!recipe.getCatalyst().matches(ItemStack.EMPTY)) {
            catalystSlot.addItemStacks(new ArrayList<>(recipe.getCatalyst().getPossible()))
                    .addTooltipCallback((slots, c) -> c.add(CATALYST));
        }

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
                            slot.addTooltipCallback((slots, c) -> c.add(MATERIAL_COMPONENT));
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
    public List<Component> getTooltipStrings(MiniaturizationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (explodeToggle.contains(mouseX, mouseY)) {
            if (!exploded) return List.of(Component.translatable("compactcrafting.jei.toggle_exploded_view"));
            else return List.of(Component.translatable("compactcrafting.jei.toggle_condensed_view"));
        }

        if (layerSwap.contains(mouseX, mouseY)) {
            if (singleLayer) return List.of(Component.translatable("compactcrafting.jei.all_layers_mode"));
            else return List.of(Component.translatable("compactcrafting.jei.single_layer_mode"));
        }

        if (layerUp.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset < recipe.getDimensions().getYsize() - 1)
                return List.of(Component.translatable("compactcrafting.jei.layer_up"));
        }

        if (layerDown.contains(mouseX, mouseY) && singleLayer) {
            if (singleLayerOffset > 0)
                return List.of(Component.translatable("compactcrafting.jei.layer_down"));
        }

        return Collections.emptyList();
    }

    @Override
    public boolean handleInput(MiniaturizationRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        if (input.getType() == InputConstants.Type.MOUSE && input.getValue() == 0) {
            SoundManager handler = Minecraft.getInstance().getSoundManager();

            if (explodeToggle.contains(mouseX, mouseY)) {
                explodeMulti = exploded ? 1.0d : 1.6d;
                exploded = !exploded;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerSwap.contains(mouseX, mouseY)) {
                singleLayer = !singleLayer;
                handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (layerUp.contains(mouseX, mouseY) && singleLayer) {
                if (singleLayerOffset < recipe.getDimensions().getYsize() - 1) {
                    handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    singleLayerOffset++;
                }
                return true;
            }

            if (layerDown.contains(mouseX, mouseY) && singleLayer) {
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
            PoseStack matrixStack,
            ResourceLocation texture,
            ScreenArea area,
            float u, float v,
            int uWidth, int vHeight,
            int textureWidth, int textureHeight) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableDepthTest();
        GuiComponent.blit(matrixStack, area.x, area.y, area.width, area.height, u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    //endregion

    @Override
    public void draw(MiniaturizationRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        AABB dims = recipe.getDimensions();

        Window mainWindow = Minecraft.getInstance().getWindow();

        drawScaledTexture(pose,
                new ResourceLocation(CompactCrafting.MOD_ID, "textures/gui/jei-arrow-field.png"),
                new ScreenArea(7, 20, 17, 22),
                0, 0, 17, 22, 17, 22);

        drawScaledTexture(pose,
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

        renderPreviewControls(pose, dims);

        if (previewLevel != null) renderRecipe(recipe, pose, dims, guiScaleFactor, scissorBounds);
    }

    private void renderRecipe(MiniaturizationRecipe recipe, PoseStack mx, AABB dims, double guiScaleFactor, ScreenArea scissorBounds) {
        try {
            GuiComponent.fill(
                    mx,
                    scissorBounds.x, scissorBounds.y,
                    scissorBounds.x + scissorBounds.width,
                    scissorBounds.height,
                    0xFF404040
            );

            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

            final double scale = Minecraft.getInstance().getWindow().getGuiScale();
            final Matrix4f matrix = mx.last().pose();
            final FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            matrix.store(buf);

            // { x, y, z }
            Vec3 translation = new Vec3(
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

            Vec3 dimsVec = new Vec3(dims.getXsize(), dims.getYsize(), dims.getZsize());
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

    private void drawActualRecipe(MiniaturizationRecipe recipe, PoseStack mx, AABB dims, MultiBufferSource.BufferSource buffers) {
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

    private void renderPreviewControls(PoseStack mx, AABB dims) {
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

        // drawScaledTexture(mx, sprites, bindPlate, 120, 0, 20, 20, 140, 20);

        mx.popPose();
    }

    private void renderRecipeLayer(MiniaturizationRecipe recipe, PoseStack mx, MultiBufferSource.BufferSource buffers, IRecipeLayer l, int layerY) {
        // Begin layer
        mx.pushPose();

        AABB layerBounds = BlockSpaceUtil.getLayerBounds(recipe.getDimensions(), layerY);
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

    private void renderComponent(PoseStack mx, MultiBufferSource.BufferSource buffers, IRecipeBlockComponent state, BlockPos filledPos) {
        // TODO - Render switching at fixed interval
        if (state.didErrorRendering())
            return;

        BlockState state1 = state.getRenderState();

        ModelData data = ModelData.EMPTY;
        if (previewLevel != null && state1.hasBlockEntity()) {
            // create fake world instance
            // get tile entity - extend EmptyBlockReader with impl
            BlockEntity be = previewLevel.getBlockEntity(filledPos);
            if (be != null)
                data = be.getModelData();
        }

        try {
            // TODO: Revisit render types
            blocks.renderSingleBlock(state1,
                    mx,
                    buffers,
                    LightTexture.FULL_SKY,
                    OverlayTexture.NO_OVERLAY,
                    data, null);
        } catch (Exception e) {
            state.markRenderingErrored();

            CompactCrafting.LOGGER.warn("Error rendering block in preview: {}", state1);
            CompactCrafting.LOGGER.error("Stack Trace", e);
        }
    }
}