package dev.compactmods.crafting.client;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.client.render.field.MiniaturizationFieldRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CompactCrafting.MOD_ID, dist = Dist.CLIENT)
public class CompactCraftingClient {

    public CompactCraftingClient(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MiniaturizationFieldRenderer::onRenderStage);
    }

}
