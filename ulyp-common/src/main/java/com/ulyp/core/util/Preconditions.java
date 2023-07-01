package com.ulyp.core.util;


/**
 * Moved from guava to avoid carrying unnecessary dependency
 */
public class Preconditions {

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, String err) {
        if (reference == null) {
            throw new NullPointerException(err);
        }
        return reference;
    }

    public static void checkState(boolean value, String err) {
        if (!value) {
            throw new IllegalStateException(err);
        }
    }
}
