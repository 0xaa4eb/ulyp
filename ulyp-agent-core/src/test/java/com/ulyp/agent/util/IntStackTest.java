package com.ulyp.agent.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class IntStackTest {

    @Test
    public void testPushPop() {
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
    public void testPushThrows() {
        IntStack stack = new IntStack();

        stack.push(5);

        stack.pop();

        Assertions.assertThrows(
                NoSuchElementException.class,
                stack::pop
        );
    }
}