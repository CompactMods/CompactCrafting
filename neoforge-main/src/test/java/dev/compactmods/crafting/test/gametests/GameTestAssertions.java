package dev.compactmods.crafting.test.gametests;

import net.minecraft.gametest.framework.GameTestAssertException;

import java.util.Objects;

public class GameTestAssertions {
    public static void assertNotNull(Object o) {
        if(o == null)
            throw new GameTestAssertException("Object is null");
    }

    public static void assertEquals(Object expected, Object actual) {
        if(!Objects.equals(expected, actual))
            throw new GameTestAssertException("Objects not equal; expected {%s} but got {%s}.".formatted(expected, actual));
    }
}
