package com.ulyp.agent.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class IntStackTest {

    @Test
    void testPushPop() {
        IntStack stack = new IntStack();

        assertTrue(stack.isEmpty());

        stack.push(5);

        assertEquals(1, stack.size());

        stack.push(6);
        stack.push(7);

        assertEquals(7, stack.pop());
        assertEquals(6, stack.pop());
        assertEquals(5, stack.pop());
    }

    @Test
    public void testPushAndGrow() {
        IntStack stack = new IntStack();

        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }

        assertEquals(100, stack.size());

        for (int i = 0; i < 100; i++) {
            assertTrue(stack.contains(i), "" + i);
        }

        for (int i = 0; i < 100; i++) {
            assertEquals(100 - i - 1, stack.pop());
        }
    }

    @Test
    public void testPushAndGrow2() {
        IntStack stack = new IntStack();

        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }

        assertEquals(100, stack.size());

        for (int i = 0; i < 100; i++) {
            assertTrue(stack.contains(i), "" + i);
        }

        for (int i = 0; i < 100; i++) {
            assertTrue(stack.popIfTop(100 - i - 1));
        }
    }

    @Test
    void testPushThrows() {
        IntStack stack = new IntStack();

        stack.push(5);

        stack.pop();

        Assertions.assertThrows(
                NoSuchElementException.class,
                stack::pop
        );
    }
}