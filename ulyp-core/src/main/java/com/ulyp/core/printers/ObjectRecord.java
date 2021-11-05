package com.ulyp.core.printers;

import com.ulyp.core.Type;

/**
 * Deserialized object representation. Depending on the printer used for serialization
 * some amount of information may (and for some data types certainly will) be lost
 *
 * For example, for strings the corresponding printer will only record first 200 symbols, for complex objects
 * only type and identity hash code are recorded.
 */
public abstract class ObjectRecord {

    private final Type type;

    protected ObjectRecord(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
