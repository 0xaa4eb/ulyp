package com.ulyp.core.printers;

import com.ulyp.core.Type;

/**
 * Deserialized object representation. Depending on the printer used for serialization
 * some amount of information may (or may not) be lost
 */
public abstract class ObjectRepresentation {

    private final Type type;

    protected ObjectRepresentation(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
