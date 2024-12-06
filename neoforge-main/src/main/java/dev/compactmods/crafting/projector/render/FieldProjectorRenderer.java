package dev.compactmods.crafting.projector.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.api.field.MiniaturizationFieldSize;
import dev.compactmods.crafting.client.render.RotationSpeed;
import dev.compactmods.crafting.client.render.field.MiniaturizationFieldRenderer;
import dev.compactmods.crafting.projector.EnumProjectorColorType;
import dev.compactmods.crafting.projector.FieldProjectorBlock;
import dev.compactmods.crafting.projector.FieldProjectorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

public class FieldProjectorRenderer implements BlockEntityRenderer<FieldProjectorEntity> {

    public static final ModelResourceLocation FIELD_DISH_RL = ModelResourceLocation.standalone(CompactCrafting.modRL("block/field_projector_dish"));

    private BakedModel bakedModelCached;

    public FieldProjectorRenderer(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(FieldProjectorEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        long gameTime = tile.getLevel().getGameTime();
        BlockState state1 = tile.getBlockState();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        VertexConsumer cutoutBlocks = buffers.getBuffer(Sheets.cutoutBlockSheet());
        BakedModel baked = this.getModel();

        matrixStack.pushPose();
        {
            matrixStack.translate(.5, 0, .5);

            double yaw = Math.sin(Math.toDegrees(gameTime) / RotationSpeed.MEDIUM.getSpeed()) * 10;

            Direction facing = state1.getValue(FieldProjectorBlock.FACING);
            if (facing != Direction.WEST) {
                float angle = facing.toYRot() - 90;
                matrixStack.mulPose(Axis.YN.rotationDegrees(angle));
            }

            float yDiskOffset = -0.66f;
            matrixStack.translate(0.0, -yDiskOffset, 0.0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees((float) yaw));
            matrixStack.translate(0.0, yDiskOffset, 0.0);

            matrixStack.translate(-.5, 0, -.5);

            int faceColor = MiniaturizationFieldRenderer.getProjectionColor(EnumProjectorColorType.PROJECTOR_FACE);
            float red = FastColor.ARGB32.red(faceColor) / 255f;
            float green = FastColor.ARGB32.green(faceColor) / 255f;
            float blue = FastColor.ARGB32.blue(faceColor) / 255f;

            // TODO - Revisit render types
            blockRenderer.getModelRenderer()
                    .renderModel(matrixStack.last(), cutoutBlocks, state1,
                            baked,
                            red,
                            green,
                            blue,
                            combinedLightIn, combinedOverlayIn, ModelData.EMPTY, null);
        }
        matrixStack.popPose();
    }

    private BakedModel getModel() {
        if (bakedModelCached == null) {
            ModelManager models = Minecraft.getInstance()
                    .getBlockRenderer()
                    .getBlockModelShaper()
                    .getModelManager();

            bakedModelCached = models.getModel(FIELD_DISH_RL);
        }

        return bakedModelCached;
    }

    @Override
    public boolean shouldRenderOffScreen(FieldProjectorEntity te) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(FieldProjectorEntity blockEntity) {
        final var p = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(p, p).inflate(20);
    }
}
