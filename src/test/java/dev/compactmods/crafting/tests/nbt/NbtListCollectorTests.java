package dev.compactmods.crafting.tests.nbt;

import dev.compactmods.crafting.CompactCrafting;
import dev.compactmods.crafting.data.NbtListCollector;
import dev.compactmods.crafting.tests.GameTestTemplates;
import dev.compactmods.crafting.tests.components.GameTestAssertions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactCrafting.MOD_ID)
public class NbtListCollectorTests {

    @GameTest(template = GameTestTemplates.EMPTY)
    public static void CanCombineLists(final GameTestHelper test) {
        ListTag list1 = new ListTag();
        ListTag list2 = new ListTag();
        list2.add(StringTag.valueOf("test"));

        final List<Tag> listCombined = NbtListCollector.combineLists(list1, list2);

        GameTestAssertions.assertEquals(1, listCombined.size());
        GameTestAssertions.assertEquals("test", listCombined.get(0).getAsString());

        test.succeed();
    }
}
