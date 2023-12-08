package dev.compactmods.crafting.tests;

import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Objects;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ModTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void canCreateItemGroup(final GameTestHelper test) {
        try {
            CompactCrafting.CCItemGroup ig = new CompactCrafting.CCItemGroup();
            Objects.requireNonNull(ig);
            ItemStack icon = ig.makeIcon();

            test.succeed();
        }

        catch(Exception e) {
            test.fail(e.getMessage());
        }
    }
}
