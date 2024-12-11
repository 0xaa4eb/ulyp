package com.ulyp.agent.util;

import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;

/**
 * Used for tracking types which constructors are currently being called.
 */
@Slf4j
public class ConstructedTypesStack {

    private final FastLookupIntStack stack = new FastLookupIntStack();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean contains(int typeId) {
        return stack.contains(typeId);
    }

    /**
     * Pushes type id which constructor has just called.
     */
    public void push(int typeId) {
        stack.push(typeId);
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Pushed (under construction) {}", typeId);
        }
    }

    /**
     * Pops type id which constructor has just completed.
     */
    public void pop(int typeId) {
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Going to pop (under construction) type id {}", typeId);
        }

        if (stack.popIfTop(typeId)) {
            return;
        }

        // The last constructor has just completed, but there is another object on the top.
        // This can happen if exception is thrown out of constructor of some other object, and it was not popped.
        if (LoggingSettings.DEBUG_ENABLED) {
            log.debug("Stack top mismatch, expected to see {} but saw {}", typeId, stack.top());
        }

        if (stack.contains(typeId)) {
            stack.popUpTo(typeId);
        }
    }
}
