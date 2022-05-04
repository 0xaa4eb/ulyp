package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class OptionalRecord extends ObjectRecord {

    private final boolean hasValue;
    private final ObjectRecord value;

    public OptionalRecord(boolean hasValue, ObjectRecord value, Type type) {
        super(type);
        this.hasValue = hasValue;
        this.value = value;
    }

    public boolean isEmpty() {
        return !hasValue;
    }

    public ObjectRecord getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "optional {" + value + "}";
    }
}
