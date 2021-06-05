package com.robotgryphon.compactcrafting.tests;

import com.robotgryphon.compactcrafting.CompactCrafting;
import com.robotgryphon.compactcrafting.Registration;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ModTests {

    @Test
    @Tag("minecraft")
    void canCreateItemGroup() {
        CompactCrafting.CCItemGroup ig = new CompactCrafting.CCItemGroup();

        Assertions.assertNotNull(ig);
        Assertions.assertDoesNotThrow(ig::makeIcon);

        ItemStack icon = ig.makeIcon();

        Assertions.assertEquals(Registration.FIELD_PROJECTOR_ITEM.get(), icon.getItem());
    }
}
