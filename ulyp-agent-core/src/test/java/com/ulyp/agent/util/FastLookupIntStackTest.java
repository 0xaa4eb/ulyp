package com.ulyp.agent.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FastLookupIntStackTest {

    @Test
    void testPopUntil() {
        FastLookupIntStack stack = new FastLookupIntStack();

        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);

        assertEquals(Arrays.asList(1, 2, 3, 4, 5), stack.toList());

        stack.popUpTo(5);

        assertEquals(Arrays.asList(1, 2, 3, 4), stack.toList());

        stack.popUpTo(3);

        assertEquals(Arrays.asList(1, 2), stack.toList());
    }

    @Test
    void testPushPopFind() {
        FastLookupIntStack stack = new FastLookupIntStack();

        stack.push(5);

        assertTrue(stack.contains(5));

        stack.push(6);
        stack.push(7);
        stack.push(8);
        stack.push(9);
        stack.push(9);
        stack.push(5);

        assertEquals(Arrays.asList(5, 6, 7, 8, 9, 9, 5), stack.toList());

        assertTrue(stack.contains(5));
        assertTrue(stack.contains(6));
        assertTrue(stack.contains(7));
        assertTrue(stack.contains(8));
        assertTrue(stack.contains(9));

        assertEquals(5, stack.pop());
        assertTrue(stack.contains(5));

        assertEquals(9, stack.pop());
        assertTrue(stack.contains(9));

        assertEquals(9, stack.pop());
        assertFalse(stack.contains(9));

        stack.pop();
        stack.pop();
        stack.pop();

        assertTrue(stack.contains(5));

        stack.pop();

        assertFalse(stack.contains(5));
        assertTrue(stack.isEmpty());
    }
}