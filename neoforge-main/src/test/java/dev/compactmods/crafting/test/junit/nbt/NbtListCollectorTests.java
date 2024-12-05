package dev.compactmods.crafting.test.junit.nbt;

import dev.compactmods.crafting.data.NbtListCollector;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NbtListCollectorTests {

    @Test
    public void CanCombineLists() {
        ListTag list1 = new ListTag();
        ListTag list2 = new ListTag();
        list2.add(StringTag.valueOf("test"));

        final List<Tag> listCombined = NbtListCollector.combineLists(list1, list2);

        Assertions.assertEquals(1, listCombined.size());
        Assertions.assertEquals("test", listCombined.get(0).getAsString());
    }
}
