package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class BooleanRepresentation extends ObjectRepresentation {

    private final boolean value;

    public BooleanRepresentation(Type type, boolean value) {
        super(type);
        this.value = value;
    }

    public boolean value() {
        return value;
    }
}
