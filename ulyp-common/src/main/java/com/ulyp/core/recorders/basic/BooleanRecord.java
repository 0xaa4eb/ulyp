package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

public class BooleanRecord extends ObjectRecord {

    private final boolean value;

    public BooleanRecord(Type type, boolean value) {
        super(type);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
