package dev.compactmods.crafting.client;

import dev.compactmods.crafting.api.CompactCrafting;
import dev.compactmods.crafting.client.render.field.MiniaturizationFieldRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CompactCrafting.MOD_ID, dist = Dist.CLIENT)
public class CompactCraftingClient {

    public CompactCraftingClient(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MiniaturizationFieldRenderer::onRenderStage);

        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onTick);
        NeoForge.EVENT_BUS.addListener(ClientEventHandler::onLevelRender);

        modBus.addListener(ClientEventHandler::registerRenderPipelines);
    }
}
