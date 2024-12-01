package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

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
