package com.robotgryphon.compactcrafting;

import com.robotgryphon.compactcrafting.client.ClientSetup;
import com.robotgryphon.compactcrafting.client.render.RenderTickCounter;
import com.robotgryphon.compactcrafting.config.ClientConfig;
import com.robotgryphon.compactcrafting.core.Registration;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "compactcrafting";

    public static ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Registration.FIELD_PROJECTOR_ITEM.get());
        }
    };

    public CompactCrafting() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        forgeBus.register(RenderTickCounter.class);
        forgeBus.register(ClientSetup.class);


        ModLoadingContext mlCtx = ModLoadingContext.get();
        mlCtx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);

        Registration.init();
    }
}
