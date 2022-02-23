package dev.compactmods.crafting.tests;

import java.util.Objects;
import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.core.CCItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class ModTests {

    @GameTest(template = "empty")
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
