package com.ulyp.core.recorders;

import com.ulyp.core.Type;

/**
 * Recorded object value. Depending on the recorder chosen for recording
 * some amount of information might (and for some data types certainly will) be lost
 *
 * For example, for strings the corresponding recorder {@link StringRecorder} will only record first 200 symbols, for complex objects
 * only type and identity hash code are recorded, see {@link IdentityRecorder}.
 */
public abstract class ObjectRecord {

    private final Type type;

    protected ObjectRecord(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    // Only used in unit tests
    public abstract String toString();
}
