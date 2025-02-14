package com.ulyp.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreconditionsTest {

    @Test
    void checkArgumentShouldPassWhenExpressionIsTrue() {
        assertDoesNotThrow(() -> Preconditions.checkArgument(true, "message"));
    }

    @Test
    void checkArgumentShouldThrowWhenExpressionIsFalse() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Preconditions.checkArgument(false, "error message")
        );
        assertEquals("error message", exception.getMessage());
    }

    @Test
    void checkNotNullShouldPassForNonNullReference() {
        String value = "test";
        String result = Preconditions.checkNotNull(value);
        assertSame(value, result);
    }

    @Test
    void checkNotNullShouldThrowForNullReference() {
        assertThrows(NullPointerException.class, () -> Preconditions.checkNotNull(null));
    }

    @Test
    void checkNotNullWithMessageShouldPassForNonNullReference() {
        String value = "test";
        String result = Preconditions.checkNotNull(value, "error message");
        assertSame(value, result);
    }

    @Test
    void checkNotNullWithMessageShouldThrowForNullReference() {
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Preconditions.checkNotNull(null, "error message")
        );
        assertEquals("error message", exception.getMessage());
    }

    @Test
    void checkStateShouldPassWhenValueIsTrue() {
        assertDoesNotThrow(() -> Preconditions.checkState(true, "message"));
    }

    @Test
    void checkStateShouldThrowWhenValueIsFalse() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> Preconditions.checkState(false, "error message")
        );
        assertEquals("error message", exception.getMessage());
    }

    @Test
    void checkNotNullShouldReturnSameReferenceForDifferentTypes() {
        Object objectValue = new Object();
        String stringValue = "test";
        Integer integerValue = 42;

        assertSame(objectValue, Preconditions.checkNotNull(objectValue));
        assertSame(stringValue, Preconditions.checkNotNull(stringValue));
        assertSame(integerValue, Preconditions.checkNotNull(integerValue));
    }
} 