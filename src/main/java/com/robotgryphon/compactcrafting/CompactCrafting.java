package com.robotgryphon.compactcrafting;

import com.robotgryphon.compactcrafting.config.ClientConfig;
import com.robotgryphon.compactcrafting.core.Registration;
import com.robotgryphon.compactcrafting.network.NetworkHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

@Mod(CompactCrafting.MOD_ID)
public class CompactCrafting {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "compactcrafting";
    public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.FIELD_PROJECTOR_ITEM.get());
        }
    };

    public CompactCrafting() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);

        Registration.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.initialize();
    }
}
