package dev.compactmods.crafting.ui.container;

import dev.compactmods.crafting.CompactCrafting;
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

    public static final RegistryObject<ContainerType<TestContainer>> TEST_CONTAINER = CONTAINERS.register("test",
            () -> IForgeContainerType.create((windowId, inv, data) -> {
                World world = inv.player.level;
                return new TestContainer(windowId, world, inv, inv.player);
            }));

    public static void init(IEventBus evt) {
        CONTAINERS.register(evt);
    }
}
