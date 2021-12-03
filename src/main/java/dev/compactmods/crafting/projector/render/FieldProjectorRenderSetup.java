package dev.compactmods.crafting.projector.render;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.Registration;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FieldProjectorRenderSetup {

    @SubscribeEvent
    public static void regRenderer(final EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerBlockEntityRenderer(Registration.FIELD_PROJECTOR_TILE.get(), FieldProjectorRenderer::new);
    }

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(Registration.FIELD_PROJECTOR_BLOCK.get(), RenderType.cutoutMipped());
    }

    @SubscribeEvent
    public static void onBlockColors(final ColorHandlerEvent.Block colors) {
        colors.getBlockColors().register(new FieldProjectorColors.Block(), Registration.FIELD_PROJECTOR_BLOCK.get());
    }

    @SubscribeEvent
    public static void onItemColors(final ColorHandlerEvent.Item itemColors) {
        itemColors.getItemColors().register(new FieldProjectorColors.Item(), Registration.FIELD_PROJECTOR_ITEM.get());
    }

    @SubscribeEvent
    public static void registerSpecialModels(final ModelRegistryEvent registryEvent) {
        ForgeModelBakery.addSpecialModel(FieldProjectorRenderer.FIELD_DISH_RL);
    }
}
