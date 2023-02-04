package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class StringObjectRecord extends ObjectRecord {

    private final String value;

    public StringObjectRecord(Type type, String value) {
        super(type);
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
