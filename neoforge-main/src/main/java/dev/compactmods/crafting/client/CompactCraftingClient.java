package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.client.render.field.MiniaturizationFieldRenderer;
import dev.compactmods.crafting.projector.model.FieldProjectorDishModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CompactCrafting.MOD_ID, dist = Dist.CLIENT)
public class CompactCraftingClient {

    public static final Identifier PROJECTOR_DISH_MODEL_ID = CompactCrafting.identifier("projector_dish");

    public static final StandaloneModelKey<BlockStateModel> PROJECTOR_DISH_MODEL_KEY
            = new StandaloneModelKey<>(PROJECTOR_DISH_MODEL_ID::toString);

    public CompactCraftingClient(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MiniaturizationFieldRenderer::onRenderStage);

        NeoForge.EVENT_BUS.addListener(ClientEventHandler::afterClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::afterParticlesRender);

        modBus.addListener(ClientEventHandler::registerSpecialRenderers);
        modBus.addListener(ClientEventHandler::registerBlockTintSources);
        modBus.addListener(ClientEventHandler::registerBlockModels);
        modBus.addListener(ClientEventHandler::registerRenderers);
        modBus.addListener(ClientEventHandler::registerRenderPipelines);
        modBus.addListener(ClientEventHandler::registerStandaloneModels);
    }

    public static Material material(String id) {
        return new Material(CompactCrafting.identifier(id));
    }
}
