package com.robotgryphon.compactcrafting.ui.container;

import com.robotgryphon.compactcrafting.CompactCrafting;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerRegistration {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, CompactCrafting.MOD_ID);

    public static final RegistryObject<ContainerType<ExampleTabbedContainer>> TEST_CONTAINER = CONTAINERS.register("test",
            () -> IForgeContainerType.create((windowId, inv, data) -> {
                World world = inv.player.level;
                return new ExampleTabbedContainer(windowId, world, inv, inv.player);
            }));

    public static void init(IEventBus evt) {
        CONTAINERS.register(evt);
    }
}
