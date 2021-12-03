package dev.compactmods.crafting.tests.data;

import java.util.List;
import dev.compactmods.crafting.data.NbtListCollector;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NbtListCollectorTests {

    @Test
    void CanCombineLists() {
        ListNBT list1 = new ListNBT();
        ListNBT list2 = new ListNBT();
        list2.add(StringNBT.valueOf("test"));

        final List<INBT> listCombined = NbtListCollector.combineLists(list1, list2);

        Assertions.assertEquals(1, listCombined.size());
        Assertions.assertEquals("test", listCombined.get(0).getAsString());
    }
}
