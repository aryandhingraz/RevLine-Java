package com.revline;

import java.util.Objects;

/**
 * Lightweight assertion utility for the in-house test runner.
 * Follows syllabus-only rules (no external testing libraries).
 */
public final class Assert {

    private Assert() {}

    @FunctionalInterface
    public interface Executable {
        void execute() throws Throwable;
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message + " (expected true but was false)");
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message + " (expected false but was true)");
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            fail(String.format("%s: expected <%s> but was <%s>", message, expected, actual));
        }
    }

    public static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            fail(String.format("%s: expected <%d> but was <%d>", message, expected, actual));
        }
    }

    public static void assertEquals(double expected, double actual, double epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            fail(String.format("%s: expected <%f> but was <%f> (tolerance: %f)", message, expected, actual, epsilon));
        }
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            fail(message + " (expected non-null object)");
        }
    }

    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            fail(message + " (expected null object but was " + obj + ")");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedException, Executable executable, String message) {
        try {
            executable.execute();
            fail(message + " (expected exception " + expectedException.getName() + " was not thrown)");
        } catch (Throwable actualException) {
            if (!expectedException.isInstance(actualException)) {
                fail(String.format("%s: expected exception %s but caught %s",
                        message, expectedException.getName(), actualException.getClass().getName()));
            }
        }
    }

    private static void fail(String error) {
        throw new AssertionError(error);
    }
}
