package dev.compactmods.crafting.client.ui.container;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerRegistration {

    public static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, CompactCrafting.MOD_ID);

    public static final RegistryObject<MenuType<TestContainer>> TEST_CONTAINER = CONTAINERS.register("test",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                Level world = inv.player.level;
                return new TestContainer(windowId, world, inv, inv.player);
            }));

    public static void init(IEventBus evt) {
        CONTAINERS.register(evt);
    }
}
