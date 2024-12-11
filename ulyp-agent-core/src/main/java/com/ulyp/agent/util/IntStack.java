package com.ulyp.agent.util;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simple int stack. Doesn't shrink by design.
 */
public class IntStack {

    private int[] array;
    private int size;

    public IntStack() {
        this.array = new int[16];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(int value) {
        ensureCapacity();
        this.array[size++] = value;
    }

    public int top() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return this.array[size - 1];
    }

    /**
     * Pops top if top matches the provided value.
     */
    public boolean popIfTop(int expectedTop) {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        int top = this.array[size - 1];
        if (top == expectedTop) {
            size--;
            return true;
        }
        return false;
    }

    public int pop() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return this.array[--size];
    }

    public boolean contains(int value) {
        int size = this.size;
        for (int i = 0; i < size; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    private void ensureCapacity() {
        if (size == array.length) {
            int[] newArray = new int[array.length * 2];
            System.arraycopy(array, 0, newArray, 0, array.length);
            this.array = newArray;
        }
    }

    @TestOnly
    public List<Integer> toList() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(array[i]);
        }
        return result;
    }
}
