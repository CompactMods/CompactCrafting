package dev.compactmods.crafting.field.events;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.field.ActiveWorldFields;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactCrafting.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ActiveWorldFieldsAttacher {

    @SubscribeEvent
    public static void onCapWorldAttach(final AttachCapabilitiesEvent<Level> event) {
        Level level = event.getObject();

        ActiveWorldFields inst = new ActiveWorldFields(level);
        final LevelFieldsProvider provider = new LevelFieldsProvider(inst);

        event.addCapability(new ResourceLocation(CompactCrafting.MOD_ID, "fields"), provider);

        event.addListener(provider::invalidate);
    }
}
