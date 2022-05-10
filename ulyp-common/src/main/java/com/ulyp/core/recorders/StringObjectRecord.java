package com.ulyp.core.recorders;

import com.ulyp.core.Type;

// TODO rename
public class StringObjectRecord extends ObjectRecord {

    private final String value;

    public StringObjectRecord(Type classDescription, String value) {
        super(classDescription);
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
