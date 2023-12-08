package dev.compactmods.crafting.projector.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCBlocks;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FieldProjectorRenderSetup {

    @SubscribeEvent
    public static void regRenderer(final EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(CCBlocks.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
    }

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CCBlocks.FIELD_PROJECTOR_BLOCK.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.getBlockColors().register(new FieldProjectorColors.Block(), CCBlocks.FIELD_PROJECTOR_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item itemColors) {
        itemColors.getItemColors().register(new FieldProjectorColors.Item(), CCItems.FIELD_PROJECTOR_ITEM.get());
    }

    @SubscribeEvent
    public static void registerSpecialModels(final ModelEvent.RegisterAdditional reg) {
        reg.register(FieldProjectorRenderer.FIELD_DISH_RL);
    }
}
