package dev.compactmods.crafting.tests.components;

import net.minecraft.gametest.framework.GameTestAssertException;

import java.util.Objects;
import java.util.function.Supplier;

@Deprecated
public class GameTestAssertions {

    public static void assertDoesNotThrow(Runnable method) {
        try {
            method.run();
        }

        catch(Exception e) {
            throw new GameTestAssertException(e.getMessage());
        }
    }

    public static void assertDoesNotThrow(Runnable method, String onFailure) {
        try {
            method.run();
        }

        catch(Exception e) {
            throw new GameTestAssertException(onFailure);
        }
    }

    public static <T> T assertDoesNotThrow(Supplier<T> method) {
        try {
            return method.get();
        }

        catch(Exception e) {
            throw new GameTestAssertException(e.getMessage());
        }
    }

    public static void assertTrue(boolean value) {
        if(!value)
            throw new GameTestAssertException("Expected true; got false.");
    }

    public static void assertTrue(boolean value, String onFailure) {
        if(!value)
            throw new GameTestAssertException(onFailure);
    }

    public static <T> void assertEquals(T t1, T t2) {
        if(!Objects.equals(t1, t2))
            throw new GameTestAssertException("Expected objects to be equal: " + t1.toString() + " vs. " + t2.toString());
    }

    public static void assertFalse(boolean value) {
        if(value)
            throw new GameTestAssertException("Expected false; got true.");
    }

    public static void assertFalse(boolean value, String onError) {
        if(value)
            throw new GameTestAssertException(onError);
    }

    public static void assertNotNull(Object obj) {
        if(obj == null)
            throw new GameTestAssertException("Object is null.");
    }
}
