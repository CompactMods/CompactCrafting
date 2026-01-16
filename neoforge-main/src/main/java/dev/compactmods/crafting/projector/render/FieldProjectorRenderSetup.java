package dev.compactmods.crafting.projector.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT)
public class FieldProjectorRenderSetup {

    @SubscribeEvent
    public static void regRenderer(final EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(CCBlocks.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
    }

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.FIELD_PROJECTOR_BLOCK.get(), ChunkSectionLayer.CUTOUT);
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(new FieldProjectorColors.Block(), CCBlocks.FIELD_PROJECTOR_BLOCK.get());
    }

//    TODO 26.1 Item Coloration
//    @SubscribeEvent
//    public static void onItemColors(final RegisterColorHandlersEvent.ItemTintSources itemColors) {
//        itemColors.register(new FieldProjectorColors.Item(), CCItems.FIELD_PROJECTOR_ITEM.get());
//    }

//    @SubscribeEvent
//    public static void registerSpecialModels(final ModelEvent. reg) {
//        reg.register(FieldProjectorRenderer.FIELD_DISH_RL);
//    }
}
