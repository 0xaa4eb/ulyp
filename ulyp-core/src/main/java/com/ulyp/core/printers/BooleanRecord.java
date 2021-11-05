package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class BooleanRecord extends ObjectRecord {

    private final boolean value;

    public BooleanRecord(Type type, boolean value) {
        super(type);
        this.value = value;
    }

    public boolean value() {
        return value;
    }
}
